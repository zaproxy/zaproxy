# Understanding Risk Descriptions in ZAP Reports

## Overview

OWASP ZAP security reports display findings using a specific notation that combines risk level with confidence level. This document explains how to interpret these risk descriptions.

## Format

Risk descriptions appear in the format: **Risk Level (Confidence Level)**

For example: `High (Medium)` or `Low (High)`

## Risk Levels

The risk level indicates the potential security impact if the vulnerability is exploited:

- **High**: Critical security vulnerability requiring immediate attention
  - Examples: SQL injection, remote code execution, authentication bypass
  - Impact: Complete system compromise possible

- **Medium**: Significant security issue that should be addressed 
  - Examples: Cross-site scripting (XSS), session fixation, information disclosure
  - Impact: Partial compromise or data exposure

- **Low**: Minor security concern with limited impact
  - Examples: Missing security headers, verbose error messages, weak SSL ciphers
  - Impact: Limited security degradation

- **Informational**: No direct security risk, provided for awareness
  - Examples: Test files present, server version disclosure, comments in code
  - Impact: No direct security impact

## Confidence Levels

The confidence level indicates how certain ZAP is that the finding is a true positive:

- **High**: Very likely a true positive (95%+ confidence)
  - The scanner has strong evidence this is a real vulnerability
  - Manual verification typically confirms these findings

- **Medium**: Probable true positive (50-95% confidence)  
  - The scanner found indicators suggesting a vulnerability
  - Manual verification recommended to confirm

- **Low**: Possible false positive (<50% confidence)
  - The scanner detected something suspicious but uncertain
  - Requires manual verification to determine if real

## Interpretation Examples

| Risk Description | Meaning | Recommended Action |
|-----------------|---------|-------------------|
| High (High) | Critical vulnerability, definitely present | Fix immediately |
| High (Medium) | Critical vulnerability, probably present | Verify and fix urgently |
| High (Low) | Critical vulnerability, needs verification | Investigate urgently |
| Medium (High) | Moderate issue, confirmed | Schedule for remediation |
| Medium (Medium) | Moderate issue, likely real | Verify and plan fix |
| Medium (Low) | Moderate issue, uncertain | Verify if real issue |
| Low (High) | Minor issue, confirmed | Fix when convenient |
| Low (Medium) | Minor issue, probably real | Consider fixing |
| Low (Low) | Minor issue, uncertain | Verify if worth fixing |
| Informational (High) | Information only, confirmed | Review for security implications |
| Informational (Medium) | Information only, likely accurate | Consider if action needed |
| Informational (Low) | Information only, uncertain | Review if relevant |

## Prioritization Guide

When reviewing ZAP reports, prioritize findings based on both risk and confidence:

1. **First Priority**: High (High), High (Medium)
2. **Second Priority**: Medium (High), High (Low) after verification
3. **Third Priority**: Medium (Medium), Low (High)
4. **Fourth Priority**: All other combinations after verification

## False Positive Handling

Findings with low confidence levels require manual verification:

1. Review the finding details and evidence
2. Manually test the potential vulnerability
3. Check if security controls mitigate the issue
4. Document if confirmed as false positive
5. Consider tuning ZAP rules if patterns emerge

## Integration with CI/CD

When using ZAP in automated pipelines, consider:

- Setting thresholds based on risk AND confidence levels
- Failing builds for High (High) findings
- Warning for High (Medium) or Medium (High)
- Logging Low confidence findings for review

## Related Documentation

- [ZAP User Guide](https://www.zaproxy.org/docs/)
- [Alert Types](https://www.zaproxy.org/docs/alerts/)
- [Report Generation](https://www.zaproxy.org/docs/desktop/ui/dialogs/reports/)