const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
    _id: { type: String, required: true },
    role: { type: String, required: true, enum: ['FREELANCER', 'CLIENT', 'ADMIN'], default: 'FREELANCER' },
    fullName: { type: String, required: true, trim: true },
    email: { type: String, required: true, unique: true, lowercase: true, trim: true },
    phone: { type: String, default: '' },
    avatarUrl: { type: String, default: '' },
    locationCountry: { type: String, default: 'United States' },
    locationState: { type: String, default: 'California' },
    locationDistrict: { type: String, default: 'San Francisco' },
    headline: { type: String, default: '' },
    bio: { type: String, default: '' },
    hourlyRate: { type: Number, default: 45.0 },
    skills: { type: String, default: '' },
    govIdType: { type: String, default: '' },
    govIdNumber: { type: String, default: '' },
    govIdDocUrl: { type: String, default: '' },
    isGovIdVerified: { type: Boolean, default: true },
    linkedInUrl: { type: String, default: '' },
    portfolioWebsite: { type: String, default: '' },
    githubUrl: { type: String, default: '' },
    passwordHash: { type: String, required: true },
    workStatus: { type: String, default: 'AVAILABLE' },
    currentActiveProjectId: { type: String, default: null },
    trustScore: { type: Number, default: 95 },
    onTimeDeliveryRate: { type: Number, default: 100 },
    rating: { type: Number, default: 5.0 },
    ratingCount: { type: Number, default: 0 },
    disputeCount: { type: Number, default: 0 },
    completedProjectsCount: { type: Number, default: 0 },
    badgeLevel: { type: String, default: 'Rising Talent' },
    companyName: { type: String, default: '' },
    industry: { type: String, default: '' },
    totalSpent: { type: Number, default: 0.0 },
    projectsPostedCount: { type: Number, default: 0 },
    createdAt: { type: Number, default: () => Date.now() },
    updatedAt: { type: Number, default: () => Date.now() }
}, {
    collection: 'users',
    versionKey: false
});

module.exports = mongoose.model('User', userSchema);
