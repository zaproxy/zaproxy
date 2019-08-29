package org.zaproxy.zap.japicmp

import japicmp.model.JApiClass
import japicmp.model.JApiCompatibility
import japicmp.model.JApiCompatibilityChange
import me.champeau.gradle.japicmp.report.Violation
import me.champeau.gradle.japicmp.report.ViolationRule

class AcceptMethodAbstractNowDefaultRule: ViolationRule {

    override fun maybeViolation(member: JApiCompatibility): Violation? {
        if (member.isBinaryCompatible() || member !is JApiClass) {
            return null
        }

        member.methods.forEach {
            it.compatibilityChanges.remove(JApiCompatibilityChange.METHOD_ABSTRACT_NOW_DEFAULT)
        }

        return null
    }
}