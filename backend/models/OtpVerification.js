const mongoose = require('mongoose');

const otpVerificationSchema = new mongoose.Schema({
    id: { 
        type: String, 
        required: true, 
        unique: true 
    },
    verification_id: { 
        type: String, 
        required: true, 
        index: true 
    },
    destination: { 
        type: String, 
        required: true, 
        index: true 
    },
    channel: { 
        type: String, 
        default: 'EMAIL' 
    },
    purpose: { 
        type: String, 
        default: 'REGISTRATION' 
    },
    otp_hash: { 
        type: String, 
        required: true 
    },
    created_at: { 
        type: Number, 
        default: () => Date.now() 
    },
    expires_at: { 
        type: Number, 
        required: true 
    },
    attempt_count: { 
        type: Number, 
        default: 0 
    },
    resend_count: { 
        type: Number, 
        default: 0 
    },
    consumed_at: { 
        type: Number, 
        default: null 
    },
    verified_at: { 
        type: Number, 
        default: null 
    }
}, {
    timestamps: true
});

module.exports = mongoose.model('OtpVerification', otpVerificationSchema);
