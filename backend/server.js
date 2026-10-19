const path = require('path');

require('dotenv').config({ path: path.resolve(__dirname, '.env') });
if (!process.env.MONGODB_URI) require('dotenv').config();
const express = require('express');
const bodyParser = require('body-parser');
const crypto = require('crypto');
const axios = require('axios');
const sqlite3 = require('sqlite3').verbose();

const jwt = require('jsonwebtoken');
const bcrypt = require('bcryptjs');
const cors = require('cors');
const mongoose = require('mongoose');
const User = require('./models/User');

const app = express();
app.use(cors());
app.use(bodyParser.json({ limit: '10mb' }));
app.use(bodyParser.urlencoded({ extended: true, limit: '10mb' }));

const JWT_SECRET = process.env.JWT_SECRET || crypto.randomBytes(32).toString('hex');
const MONGODB_URI = process.env.MONGODB_URI;
const MONGODB_DATABASE = process.env.MONGODB_DATABASE || 'gigverse';

if (!MONGODB_URI) {
    console.error('FATAL: MONGODB_URI environment variable is missing.');
    process.exit(1);
}

// Connect explicitly to the authoritative MongoDB Atlas database 'gigverse'
mongoose.connect(MONGODB_URI, {
    dbName: MONGODB_DATABASE,
    maxPoolSize: 10,
    serverSelectionTimeoutMS: 10000,
    socketTimeoutMS: 45000,
}).then(() => {
    console.log(`Authoritative MongoDB Atlas connected successfully to database: "${mongoose.connection.name}"`);
}).catch(err => {
    console.error('FATAL: MongoDB Atlas connection failed:', err.message);
    process.exit(1);
});

// Preserve existing SQLite DB for OTP verifications
const dbPath = path.resolve(__dirname, 'otp_backend.db');
const db = new sqlite3.Database(dbPath);

db.serialize(() => {
    db.run(`CREATE TABLE IF NOT EXISTS otp_verifications (
        id TEXT PRIMARY KEY,
        verification_id TEXT,
        destination TEXT,
        channel TEXT,
        purpose TEXT,
        otp_hash TEXT,
        created_at INTEGER,
        expires_at INTEGER,
        attempt_count INTEGER DEFAULT 0,
        resend_count INTEGER DEFAULT 0,
        consumed_at INTEGER,
        verified_at INTEGER
    )`);
});

// Middleware
const authenticateToken = (req, res, next) => {
    const authHeader = req.headers['authorization'];
    const token = authHeader && authHeader.split(' ')[1];
    if (!token) return res.status(401).json({ success: false, error: 'Missing token' });

    jwt.verify(token, JWT_SECRET, (err, user) => {
        if (err) return res.status(403).json({ success: false, error: 'Invalid or expired token' });
        req.user = user;
        next();
    });
};

const hashOtp = (otp) => {
    return crypto.createHash('sha256').update(otp).digest('hex');
};

const generateNumericOtp = () => {
    return Math.floor(100000 + Math.random() * 900000).toString();
};

function verifyPassword(rawPassword, storedHash) {
    if (!rawPassword || !storedHash) return false;
    try {
        if (storedHash.startsWith('pbkdf2:')) {
            const parts = storedHash.split(':');
            if (parts.length === 4) {
                const iter = parseInt(parts[1], 10);
                const salt = Buffer.from(parts[2], 'hex');
                const expected = Buffer.from(parts[3], 'hex');
                const derived = crypto.pbkdf2Sync(rawPassword, salt, iter, expected.length, 'sha256');
                return crypto.timingSafeEqual(derived, expected);
            }
        }
        if (storedHash.startsWith('$2a$') || storedHash.startsWith('$2b$')) {
            return bcrypt.compareSync(rawPassword, storedHash);
        }
        if (storedHash.length === 64) {
            const sha = crypto.createHash('sha256').update(rawPassword).digest('hex');
            return crypto.timingSafeEqual(Buffer.from(sha), Buffer.from(storedHash.toLowerCase()));
        }
    } catch (_) {
        return false;
    }
    return false;
}

function sanitizeUser(userDoc) {
    const obj = userDoc.toObject ? userDoc.toObject() : { ...userDoc };
    delete obj.passwordHash;
    obj.id = obj._id;
    return obj;
}

// Health Check
app.get('/api/health', (req, res) => {
    const isConnected = mongoose.connection.readyState === 1;
    res.json({
        status: isConnected ? 'healthy' : 'degraded',
        database: mongoose.connection.name || MONGODB_DATABASE,
        mongodb_connected: isConnected,
        timestamp: Date.now()
    });
});

// Cloud Registration Endpoint -> MongoDB Atlas 'gigverse.users'
app.post('/api/auth/register', async (req, res) => {
    try {
        const {
            role = 'FREELANCER',
            fullName,
            email,
            password,
            phone = '',
            avatarUrl = '',
            locationCountry = 'United States',
            locationState = 'California',
            locationDistrict = 'San Francisco',
            headline = '',
            bio = '',
            hourlyRate = 45.0,
            skills = 'Kotlin, Android, Jetpack Compose',
            govIdType = 'National ID / Passport',
            govIdNumber = 'US-ID-884920',
            govIdDocUrl = '',
            isGovIdVerified = true,
            linkedInUrl = '',
            portfolioWebsite = '',
            githubUrl = '',
            companyName = '',
            industry = 'Software & AI Tech'
        } = req.body;

        if (!fullName || !fullName.trim()) {
            return res.status(400).json({ success: false, error: 'Please enter your full name.' });
        }
        if (!email || !email.trim() || !email.includes('@')) {
            return res.status(400).json({ success: false, error: 'Please enter a valid email address.' });
        }
        if (!password || password.trim().length < 6) {
            return res.status(400).json({ success: false, error: 'Please enter a secure password (at least 6 characters).' });
        }

        const normalizedRole = role.toUpperCase();
        if (!['FREELANCER', 'CLIENT', 'ADMIN'].includes(normalizedRole)) {
            return res.status(400).json({ success: false, error: 'Invalid user role specified.' });
        }

        const normalizedEmail = email.trim().toLowerCase();

        // Enforce duplicate check in authoritative MongoDB database
        const existing = await User.findOne({ email: normalizedEmail });
        if (existing) {
            return res.status(409).json({
                success: false,
                error: 'An account with this email already exists. Please log in.'
            });
        }

        const passwordHash = await bcrypt.hash(password.trim(), 10);
        const userId = (normalizedRole === 'CLIENT' ? 'client_' : 'freelancer_') + Date.now();

        const newUser = new User({
            _id: userId,
            role: normalizedRole,
            fullName: fullName.trim(),
            email: normalizedEmail,
            phone: (phone || '').trim(),
            avatarUrl: avatarUrl || '',
            locationCountry: locationCountry || 'United States',
            locationState: locationState || 'California',
            locationDistrict: locationDistrict || 'San Francisco',
            headline: headline || '',
            bio: bio || '',
            hourlyRate: typeof hourlyRate === 'number' ? hourlyRate : 45.0,
            skills: skills || '',
            govIdType: govIdType || 'National ID / Passport',
            govIdNumber: govIdNumber || '',
            govIdDocUrl: govIdDocUrl || '',
            isGovIdVerified: isGovIdVerified !== undefined ? isGovIdVerified : true,
            linkedInUrl: linkedInUrl || '',
            portfolioWebsite: portfolioWebsite || '',
            githubUrl: githubUrl || '',
            passwordHash: passwordHash,
            workStatus: 'AVAILABLE',
            currentActiveProjectId: null,
            trustScore: (govIdDocUrl && avatarUrl) ? 98 : 94,
            onTimeDeliveryRate: 98,
            rating: 5.0,
            ratingCount: 1,
            disputeCount: 0,
            completedProjectsCount: 0,
            badgeLevel: normalizedRole === 'FREELANCER' ? 'Rising Talent' : '',
            companyName: companyName || '',
            industry: industry || '',
            totalSpent: 0.0,
            projectsPostedCount: 0,
            createdAt: Date.now(),
            updatedAt: Date.now()
        });

        // Write to MongoDB Atlas
        await newUser.save();

        const token = jwt.sign(
            { id: userId, role: normalizedRole, email: normalizedEmail },
            JWT_SECRET,
            { expiresIn: '30d' }
        );

        return res.status(201).json({
            success: true,
            token: token,
            message: `Welcome to GIGVERSE, ${fullName.trim()}! Account created and verified in cloud.`,
            user: sanitizeUser(newUser)
        });
    } catch (err) {
        if (err.code === 11000) {
            return res.status(409).json({
                success: false,
                error: 'An account with this email already exists. Please log in.'
            });
        }
        console.error('Registration persistence error:', err.message);
        return res.status(500).json({
            success: false,
            error: 'Server error during registration. Cloud persistence failed.'
        });
    }
});

// Cloud Login Endpoint -> MongoDB Atlas 'gigverse.users'
app.post('/api/auth/login', async (req, res) => {
    try {
        const { email, password, role } = req.body;
        if (!email || !email.trim()) {
            return res.status(400).json({ success: false, error: 'Please enter your email address.' });
        }
        if (!password || !password.trim()) {
            return res.status(400).json({ success: false, error: 'Please enter your password.' });
        }

        const normalizedEmail = email.trim().toLowerCase();
        const user = await User.findOne({ email: normalizedEmail });

        if (!user) {
            return res.status(401).json({
                success: false,
                error: 'No account found with this email. Please check your credentials or register.'
            });
        }

        if (role && role !== 'ADMIN' && user.role.toUpperCase() !== role.toUpperCase()) {
            const expectedRole = user.role.toUpperCase() === 'FREELANCER' ? 'Freelancer' : 'Client';
            return res.status(403).json({
                success: false,
                error: `This email is registered as a ${expectedRole}. Please switch to the ${expectedRole} tab to sign in.`
            });
        }

        const isValidPassword = verifyPassword(password.trim(), user.passwordHash);
        if (!isValidPassword) {
            return res.status(401).json({
                success: false,
                error: 'Incorrect password. Please try again.'
            });
        }

        // Seamlessly upgrade legacy PBKDF2 hash to bcrypt
        if (user.passwordHash.startsWith('pbkdf2:') || user.passwordHash.length === 64) {
            try {
                user.passwordHash = await bcrypt.hash(password.trim(), 10);
                user.updatedAt = Date.now();
                await user.save();
            } catch (_) {}
        }

        const token = jwt.sign(
            { id: user._id, role: user.role, email: user.email },
            JWT_SECRET,
            { expiresIn: '30d' }
        );

        return res.status(200).json({
            success: true,
            token: token,
            message: `Welcome back, ${user.fullName}!`,
            user: sanitizeUser(user)
        });
    } catch (err) {
        console.error('Login error:', err.message);
        return res.status(500).json({
            success: false,
            error: 'Authentication failed due to server error.'
        });
    }
});

// Fetch user profile from MongoDB
app.get('/api/users/by-email/:email', async (req, res) => {
    try {
        const normalizedEmail = (req.params.email || '').trim().toLowerCase();
        const user = await User.findOne({ email: normalizedEmail });
        if (!user) return res.status(404).json({ success: false, error: 'User not found' });
        return res.json({ success: true, user: sanitizeUser(user) });
    } catch (err) {
        return res.status(500).json({ success: false, error: 'Database query error' });
    }
});

app.get('/api/users/:id', authenticateToken, async (req, res) => {
    try {
        const user = await User.findById(req.params.id);
        if (!user) return res.status(404).json({ success: false, error: 'User not found' });
        return res.json({ success: true, user: sanitizeUser(user) });
    } catch (err) {
        return res.status(500).json({ success: false, error: 'Database query error' });
    }
});

// Existing OTP routes (Brevo & SQLite)
app.post('/api/otp/send', async (req, res) => {
    const { destination, channel } = req.body;
    if (!destination || !channel) return res.status(400).json({ success: false, error: 'Missing destination or channel' });

    const thirtySecondsAgo = Date.now() - 30000;
    db.get(`SELECT * FROM otp_verifications WHERE destination = ? AND purpose = 'REGISTRATION' ORDER BY created_at DESC LIMIT 1`, [destination], async (err, row) => {
        if (err) return res.status(500).json({ success: false, error: 'Database error' });
        
        let resendCount = 0;
        if (row) {
            if (row.created_at > thirtySecondsAgo) {
                return res.status(429).json({ success: false, error: 'Please wait 30 seconds before resending.' });
            }
            resendCount = row.resend_count + 1;
        }

        const otp = generateNumericOtp();
        const otpHash = hashOtp(otp);
        const verificationId = `vid_${Date.now()}`;
        const createdAt = Date.now();
        const expiresAt = createdAt + (5 * 60 * 1000);

        db.run(`INSERT INTO otp_verifications (id, verification_id, destination, channel, purpose, otp_hash, created_at, expires_at, resend_count) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`,
            [`otp_${Date.now()}`, verificationId, destination, channel, 'REGISTRATION', otpHash, createdAt, expiresAt, resendCount],
            async (insertErr) => {
                if (insertErr) return res.status(500).json({ success: false, error: 'Database error' });

                if (channel === 'EMAIL') {
                    if (!process.env.BREVO_API_KEY) {
                        return res.json({ success: true, verification_id: verificationId, dev_otp: otp });
                    }
                    try {
                        await axios.post('https://api.brevo.com/v3/smtp/email', {
                            sender: { name: process.env.SMTP_FROM_NAME || 'GIGVERSE Security', email: process.env.SMTP_FROM_EMAIL || 'security@gigverse.com' },
                            to: [{ email: destination, name: 'GIGVERSE User' }],
                            subject: 'GIGVERSE Verification Code',
                            htmlContent: `<html><body><h3>Your Verification Code is: ${otp}</h3><p>It is valid for 5 minutes.</p></body></html>`
                        }, { headers: { 'api-key': process.env.BREVO_API_KEY } });
                        return res.json({ success: true, verification_id: verificationId });
                    } catch (e) {
                        return res.status(500).json({ success: false, error: 'Email service error' });
                    }
                } else {
                    return res.status(400).json({ success: false, error: 'Invalid channel' });
                }
            }
        );
    });
});

app.post('/api/otp/verify', (req, res) => {
    const { verification_id, otp } = req.body;
    if (!verification_id || !otp) return res.status(400).json({ success: false, error: 'Missing verification_id or otp' });

    db.get(`SELECT * FROM otp_verifications WHERE verification_id = ? AND purpose = 'REGISTRATION'`, [verification_id], (err, row) => {
        if (err || !row) return res.status(404).json({ success: false, error: 'Verification not found' });

        if (row.consumed_at) return res.status(400).json({ success: false, error: 'OTP is already consumed' });
        if (Date.now() > row.expires_at) return res.status(400).json({ success: false, error: 'OTP has expired' });
        if (row.attempt_count >= 5) return res.status(400).json({ success: false, error: 'Too many failed attempts' });

        const inputHash = hashOtp(otp);
        if (row.otp_hash === inputHash) {
            db.run(`UPDATE otp_verifications SET consumed_at = ?, verified_at = ?, attempt_count = attempt_count + 1 WHERE verification_id = ?`,
                [Date.now(), Date.now(), verification_id],
                (updateErr) => {
                    if (updateErr) return res.status(500).json({ success: false, error: 'Database error' });
                    return res.json({ success: true, verified: true });
                }
            );
        } else {
            db.run(`UPDATE otp_verifications SET attempt_count = attempt_count + 1 WHERE verification_id = ?`, [verification_id], () => {
                return res.status(400).json({ success: false, error: 'Incorrect OTP' });
            });
        }
    });
});

app.delete('/api/account', authenticateToken, async (req, res) => {
    try {
        const userId = req.user.id;
        await User.findByIdAndDelete(userId);
        return res.json({ success: true, message: 'Account deleted securely from cloud database' });
    } catch (err) {
        return res.status(500).json({ success: false, error: 'Failed to delete account' });
    }
});

const HOST = process.env.HOST || '0.0.0.0';
const PORT = process.env.PORT || 5000;
const server = app.listen(PORT, HOST, () => {
    console.log(`GigVerse Production Backend running on http://${HOST}:${PORT}`);
});

module.exports = { app, server };

