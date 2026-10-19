const http = require('http');
const assert = require('assert');

const BASE_URL = 'http://localhost:5000';

function request(method, path, data = null, headers = {}) {
    return new Promise((resolve, reject) => {
        const url = new URL(path, BASE_URL);
        const options = {
            hostname: url.hostname,
            port: url.port,
            path: url.pathname,
            method: method,
            headers: {
                'Content-Type': 'application/json',
                ...headers
            }
        };

        const req = http.request(options, (res) => {
            let body = '';
            res.on('data', chunk => body += chunk);
            res.on('end', () => {
                let parsed = null;
                try {
                    parsed = JSON.parse(body);
                } catch (_) {
                    parsed = body;
                }
                resolve({ status: res.statusCode, data: parsed });
            });
        });

        req.on('error', reject);
        if (data) {
            req.write(JSON.stringify(data));
        }
        req.end();
    });
}

async function runTests() {
    console.log('--- Starting Backend & MongoDB Atlas Verification Suite ---');

    // 1. Health check
    console.log('[Test 1] Checking GET /api/health...');
    const health = await request('GET', '/api/health');
    assert.strictEqual(health.status, 200, 'Health endpoint must return 200');
    assert.strictEqual(health.data.mongodb_connected, true, 'MongoDB Atlas must be connected');
    assert.strictEqual(health.data.database, 'gigverse', 'Database name must be gigverse');
    console.log('✓ Health check passed. Connected to MongoDB database: "gigverse"');

    // 2. Missing fields in registration
    console.log('[Test 2] Testing registration validation (missing email)...');
    const invalidReg = await request('POST', '/api/auth/register', {
        role: 'FREELANCER',
        fullName: 'Test User',
        password: 'Password123!'
    });
    assert.strictEqual(invalidReg.status, 400, 'Missing email must return 400');
    assert.strictEqual(invalidReg.data.success, false);
    console.log('✓ Validation rejected missing email correctly');

    // 3. Valid Freelancer Registration
    const testTimestamp = Date.now();
    const testEmail = `test.dev.${testTimestamp}@gigverse.cloud`;
    const testPassword = 'SecurePassword2026!';
    console.log(`[Test 3] Registering new Freelancer user: ${testEmail}...`);
    const regRes = await request('POST', '/api/auth/register', {
        role: 'FREELANCER',
        fullName: 'Atlas Test Freelancer',
        email: testEmail,
        password: testPassword,
        phone: '+1 555-0199',
        locationCountry: 'United States',
        locationState: 'California',
        locationDistrict: 'San Francisco',
        headline: 'Senior Cloud Systems Architect',
        skills: 'Kotlin, Jetpack Compose, MongoDB, Node.js',
        hourlyRate: 85.0
    });

    assert.strictEqual(regRes.status, 201, 'Registration must return 201 Created');
    assert.strictEqual(regRes.data.success, true);
    assert.ok(regRes.data.token, 'Must return JWT token');
    assert.ok(regRes.data.user, 'Must return user object');
    assert.strictEqual(regRes.data.user.email, testEmail);
    assert.strictEqual(regRes.data.user.role, 'FREELANCER');
    assert.strictEqual(regRes.data.user.passwordHash, undefined, 'Must NEVER return passwordHash');
    console.log('✓ Registration succeeded and written to MongoDB Atlas users collection');

    // 4. Duplicate Registration
    console.log('[Test 4] Testing duplicate registration rejection...');
    const dupRes = await request('POST', '/api/auth/register', {
        role: 'FREELANCER',
        fullName: 'Atlas Test Freelancer Duplicate',
        email: testEmail,
        password: testPassword
    });
    assert.strictEqual(dupRes.status, 409, 'Duplicate email must return 409 Conflict');
    assert.strictEqual(dupRes.data.success, false);
    assert.ok(dupRes.data.error.includes('already exists'), 'Error must inform user account already exists');
    console.log('✓ Duplicate registration rejected correctly with 409 Conflict');

    // 5. Successful Login
    console.log('[Test 5] Testing login with valid credentials...');
    const loginRes = await request('POST', '/api/auth/login', {
        email: testEmail,
        password: testPassword,
        role: 'FREELANCER'
    });
    assert.strictEqual(loginRes.status, 200, 'Login must return 200');
    assert.strictEqual(loginRes.data.success, true);
    assert.ok(loginRes.data.token, 'Login must return JWT token');
    assert.strictEqual(loginRes.data.user.email, testEmail);
    assert.strictEqual(loginRes.data.user.passwordHash, undefined, 'Must NEVER return passwordHash on login');
    console.log('✓ Login succeeded with valid credentials');

    // 6. Login with incorrect password
    console.log('[Test 6] Testing login with incorrect password...');
    const badPassRes = await request('POST', '/api/auth/login', {
        email: testEmail,
        password: 'WrongPassword!',
        role: 'FREELANCER'
    });
    assert.strictEqual(badPassRes.status, 401, 'Incorrect password must return 401');
    assert.strictEqual(badPassRes.data.success, false);
    console.log('✓ Incorrect password rejected correctly with 401');

    // 7. Login with mismatched role
    console.log('[Test 7] Testing login with mismatched role (CLIENT tab)...');
    const wrongRoleRes = await request('POST', '/api/auth/login', {
        email: testEmail,
        password: testPassword,
        role: 'CLIENT'
    });
    assert.strictEqual(wrongRoleRes.status, 403, 'Role mismatch must return 403');
    assert.strictEqual(wrongRoleRes.data.success, false);
    assert.ok(wrongRoleRes.data.error.includes('registered as a Freelancer'), 'Must guide user to correct tab');
    console.log('✓ Role mismatch enforced correctly with 403');

    // 8. Test Client Registration
    const clientEmail = `client.${testTimestamp}@gigverse.cloud`;
    console.log(`[Test 8] Registering new Client user: ${clientEmail}...`);
    const clientReg = await request('POST', '/api/auth/register', {
        role: 'CLIENT',
        fullName: 'Atlas Test Client',
        companyName: 'GigVerse Labs Inc.',
        industry: 'Cloud Software',
        email: clientEmail,
        password: testPassword,
        phone: '+1 555-0200',
        locationCountry: 'United States',
        locationState: 'California',
        locationDistrict: 'San Francisco'
    });
    assert.strictEqual(clientReg.status, 201);
    assert.strictEqual(clientReg.data.user.role, 'CLIENT');
    assert.strictEqual(clientReg.data.user.companyName, 'GigVerse Labs Inc.');
    console.log('✓ Client registration succeeded and persisted to MongoDB Atlas');

    // 9. Client Login
    console.log('[Test 9] Testing Client login...');
    const clientLogin = await request('POST', '/api/auth/login', {
        email: clientEmail,
        password: testPassword,
        role: 'CLIENT'
    });
    assert.strictEqual(clientLogin.status, 200);
    assert.strictEqual(clientLogin.data.user.role, 'CLIENT');
    console.log('✓ Client login succeeded');

    console.log('\n======================================================');
    console.log('🎉 ALL BACKEND & MONGODB ATLAS INTEGRATION TESTS PASSED!');
    console.log('======================================================\n');
}

module.exports = { runTests };

if (require.main === module) {
    runTests().catch(err => {
        console.error('Test Suite Failed:', err);
        process.exit(1);
    });
}
