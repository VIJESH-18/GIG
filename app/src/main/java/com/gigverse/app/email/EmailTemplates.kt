package com.gigverse.app.email

/**
 * Pre-designed HTML Email Templates for all key GIGVERSE system features.
 * Formatted with inline CSS using GIGVERSE brand color palette:
 * - Violet Primary: #7C3AED
 * - Cyan Accent: #06B6D4
 * - Emerald Trust: #10B981
 * - Live Pulse Red: #EF4444
 * - Dark Surface: #0F172A / #0B0F17
 */
object EmailTemplates {

    fun getSecurityOtpTemplate(otpCode: String, recipientName: String = "User"): Pair<String, String> {
        val subject = "[GIGVERSE] Security Passkey Verification Code: $otpCode"
        val html = """
            <!DOCTYPE html>
            <html>
            <head><meta charset="utf-8"><title>Security Verification</title></head>
            <body style="margin: 0; padding: 0; background-color: #0B0F17; font-family: Arial, sans-serif; color: #F8FAFC;">
              <table width="100%" border="0" cellspacing="0" cellpadding="0" style="background-color: #0B0F17; padding: 40px 10px;">
                <tr>
                  <td align="center">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0" style="max-width: 580px; background-color: #0F172A; border: 1px solid #1E293B; border-radius: 16px; overflow: hidden;">
                      <tr>
                        <td style="padding: 30px; background: linear-gradient(135deg, #1E1B4B 0%, #0F172A 100%); text-align: center;">
                          <div style="font-size: 24px; font-weight: 900; letter-spacing: 2px; color: #FFFFFF;">GIGVERSE</div>
                          <div style="font-size: 11px; font-weight: 700; letter-spacing: 1.5px; color: #06B6D4; margin-top: 6px; text-transform: uppercase;">Security & Passkey Authorization</div>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding: 36px 30px;">
                          <h2 style="margin: 0 0 12px 0; font-size: 20px; font-weight: 800; color: #F8FAFC;">Hello $recipientName,</h2>
                          <p style="margin: 0 0 24px 0; font-size: 14px; line-height: 1.6; color: #94A3B8;">
                            You recently requested a security passkey verification for your <strong>GIGVERSE Account</strong>. Use the 6-digit one-time code below to authorize your session:
                          </p>
                          <table width="100%" border="0" cellspacing="0" cellpadding="0" style="margin-bottom: 28px;">
                            <tr>
                              <td align="center" style="padding: 20px; background: rgba(124, 58, 237, 0.12); border: 1.5px dashed #7C3AED; border-radius: 12px;">
                                <div style="font-size: 11px; font-weight: 700; color: #94A3B8; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 6px;">One-Time Security Code</div>
                                <div style="font-size: 36px; font-weight: 900; letter-spacing: 8px; color: #22D3EE; font-family: monospace;">$otpCode</div>
                                <div style="font-size: 11px; color: #EF4444; margin-top: 6px;">⏱️ Valid for 10 minutes. Do NOT share this code with anyone.</div>
                              </td>
                            </tr>
                          </table>
                          <p style="margin: 0 0 16px 0; font-size: 13px; color: #94A3B8;">
                            If you did not initiate this request, please update your security settings immediately.
                          </p>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding: 20px; background-color: #0B0F17; border-top: 1px solid #1E293B; text-align: center; font-size: 12px; color: #64748B;">
                          GIGVERSE Security ! &bull; Autonomous Marketplace Engine
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
        """.trimIndent()
        return Pair(subject, html)
    }

    fun getWelcomeTemplate(userName: String, role: String): Pair<String, String> {
        val subject = "Welcome to GIGVERSE! Your $role Account is Active 🚀"
        val html = """
            <!DOCTYPE html>
            <html>
            <head><meta charset="utf-8"><title>Welcome</title></head>
            <body style="margin: 0; padding: 0; background-color: #0B0F17; font-family: Arial, sans-serif; color: #F8FAFC;">
              <table width="100%" border="0" cellspacing="0" cellpadding="0" style="background-color: #0B0F17; padding: 40px 10px;">
                <tr>
                  <td align="center">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0" style="max-width: 580px; background-color: #0F172A; border: 1px solid #1E293B; border-radius: 16px; overflow: hidden;">
                      <tr>
                        <td style="padding: 30px; background: linear-gradient(135deg, #06B6D4 0%, #7C3AED 100%); text-align: center;">
                          <div style="font-size: 28px; font-weight: 900; color: #FFFFFF; letter-spacing: 2px;">GIGVERSE</div>
                          <div style="font-size: 13px; font-weight: 700; color: #E0F2FE; margin-top: 4px;">Welcome $userName ($role)</div>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding: 36px 30px;">
                          <h2 style="margin: 0 0 14px 0; font-size: 22px; font-weight: 800; color: #F8FAFC;">Welcome to Autonomous Freelancing! ⚡</h2>
                          <p style="margin: 0 0 20px 0; font-size: 14px; line-height: 1.6; color: #94A3B8;">
                            Your GIGVERSE account has been successfully initialized. You can now access live auction bidding, milestone escrows, and your personal Gemini AI Copilot.
                          </p>
                          <table width="100%" border="0" cellspacing="0" cellpadding="0" style="margin-bottom: 24px; background: #1E293B; border-radius: 12px; padding: 16px;">
                            <tr><td style="padding: 6px; color: #34D399; font-weight: 700;">✅ Account Verification: Passed</td></tr>
                            <tr><td style="padding: 6px; color: #67E8F9;">🛡️ Escrow Protection: Active</td></tr>
                            <tr><td style="padding: 6px; color: #E9D5FF;">🤖 Gemini AI Copilot: Ready</td></tr>
                          </table>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding: 20px; background-color: #0B0F17; text-align: center; font-size: 12px; color: #64748B;">
                          GIGVERSE Security ! &bull; Sender: godfrey.cs23@krct.ac.in
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
        """.trimIndent()
        return Pair(subject, html)
    }

    fun getBidAlertTemplate(projectTitle: String, freelancerName: String, bidAmount: Double, deliveryDays: Int): Pair<String, String> {
        val subject = "🔥 New Competitive Bid Received: $projectTitle"
        val html = """
            <!DOCTYPE html>
            <html>
            <head><meta charset="utf-8"><title>Bid Alert</title></head>
            <body style="margin: 0; padding: 0; background-color: #0B0F17; font-family: Arial, sans-serif; color: #F8FAFC;">
              <table width="100%" border="0" cellspacing="0" cellpadding="0" style="background-color: #0B0F17; padding: 40px 10px;">
                <tr>
                  <td align="center">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0" style="max-width: 580px; background-color: #0F172A; border: 1px solid #1E293B; border-radius: 16px; overflow: hidden;">
                      <tr>
                        <td style="padding: 24px 30px; background-color: #1E293B; border-bottom: 1px solid #334155;">
                          <span style="font-size: 12px; font-weight: 800; color: #22D3EE; text-transform: uppercase;">Live Auction Floor Notification</span>
                          <h2 style="margin: 6px 0 0 0; font-size: 18px; font-weight: 800; color: #F8FAFC;">New Proposal Submitted</h2>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding: 30px;">
                          <div style="background: rgba(6, 182, 212, 0.08); border: 1px solid #06B6D4; border-radius: 12px; padding: 20px; margin-bottom: 20px;">
                            <div style="font-size: 16px; font-weight: 800; color: #FFFFFF; margin-bottom: 8px;">$projectTitle</div>
                            <div style="font-size: 13px; color: #94A3B8;">Freelancer: <strong style="color: #67E8F9;">$freelancerName</strong></div>
                            <div style="font-size: 13px; color: #94A3B8; margin-top: 4px;">Bid Amount: <strong style="color: #34D399; font-size: 16px;">$$bidAmount USD</strong></div>
                            <div style="font-size: 13px; color: #94A3B8; margin-top: 4px;">Delivery Timeline: <strong>$deliveryDays Days</strong></div>
                          </div>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding: 16px; background-color: #0B0F17; text-align: center; font-size: 11px; color: #64748B;">
                          GIGVERSE Security ! &bull; Automated Bidding Engine
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
        """.trimIndent()
        return Pair(subject, html)
    }

    fun getEscrowLockTemplate(projectTitle: String, milestoneStage: Int, amount: Double): Pair<String, String> {
        val subject = "🔐 Escrow Lock Confirmed: Stage $milestoneStage ($$amount USD)"
        val html = """
            <!DOCTYPE html>
            <html>
            <head><meta charset="utf-8"><title>Escrow Locked</title></head>
            <body style="margin: 0; padding: 0; background-color: #0B0F17; font-family: Arial, sans-serif; color: #F8FAFC;">
              <table width="100%" border="0" cellspacing="0" cellpadding="0" style="background-color: #0B0F17; padding: 40px 10px;">
                <tr>
                  <td align="center">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0" style="max-width: 580px; background-color: #0F172A; border: 1px solid #1E293B; border-radius: 16px; overflow: hidden;">
                      <tr>
                        <td style="padding: 24px 30px; background: linear-gradient(135deg, #059669 0%, #10B981 100%); text-align: center;">
                          <div style="font-size: 22px; font-weight: 900; color: #FFFFFF;">Escrow Funding Locked 🛡️</div>
                          <div style="font-size: 12px; color: #D1FAE5; margin-top: 4px;">Milestone Stage $milestoneStage Authorized</div>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding: 30px;">
                          <p style="margin: 0 0 20px 0; font-size: 14px; color: #94A3B8;">
                            Funds for <strong>$projectTitle</strong> (Stage $milestoneStage) have been locked into the GIGVERSE Escrow Vault.
                          </p>
                          <table width="100%" border="0" cellspacing="0" cellpadding="0" style="background: #1E293B; border-radius: 10px; padding: 16px;">
                            <tr><td style="color: #94A3B8; font-size: 13px;">Amount Locked:</td><td style="color: #34D399; font-weight: 800; font-size: 16px; text-align: right;">$$amount USD</td></tr>
                          </table>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding: 16px; background-color: #0B0F17; text-align: center; font-size: 11px; color: #64748B;">
                          GIGVERSE Security ! &bull; Zero-Commission Smart Escrow
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
        """.trimIndent()
        return Pair(subject, html)
    }

    fun getDisputeNoticeTemplate(caseId: String, projectTitle: String, reason: String): Pair<String, String> {
        val subject = "⚠️ Governance Alert: Dispute Open ($caseId)"
        val html = """
            <!DOCTYPE html>
            <html>
            <head><meta charset="utf-8"><title>Dispute Notice</title></head>
            <body style="margin: 0; padding: 0; background-color: #0B0F17; font-family: Arial, sans-serif; color: #F8FAFC;">
              <table width="100%" border="0" cellspacing="0" cellpadding="0" style="background-color: #0B0F17; padding: 40px 10px;">
                <tr>
                  <td align="center">
                    <table width="100%" border="0" cellspacing="0" cellpadding="0" style="max-width: 580px; background-color: #0F172A; border: 1px solid #1E293B; border-radius: 16px; overflow: hidden;">
                      <tr>
                        <td style="padding: 24px 30px; background: linear-gradient(135deg, #DC2626 0%, #EF4444 100%); text-align: center;">
                          <div style="font-size: 20px; font-weight: 900; color: #FFFFFF;">Platform Governance Notice ⚖️</div>
                          <div style="font-size: 12px; color: #FEE2E2; margin-top: 4px;">Mediation Case #$caseId</div>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding: 30px;">
                          <div style="background: rgba(239, 68, 68, 0.1); border: 1px solid #EF4444; border-radius: 10px; padding: 16px;">
                            <div style="font-size: 14px; font-weight: 700; color: #FCA5A5;">Project: $projectTitle</div>
                            <div style="font-size: 13px; color: #CBD5E1; margin-top: 6px;">Reason: $reason</div>
                          </div>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding: 16px; background-color: #0B0F17; text-align: center; font-size: 11px; color: #64748B;">
                          GIGVERSE Security ! &bull; Trust & Safety Mediation
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
        """.trimIndent()
        return Pair(subject, html)
    }
}
