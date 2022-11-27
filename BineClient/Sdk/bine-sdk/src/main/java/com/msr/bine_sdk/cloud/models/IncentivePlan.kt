package com.msr.bine_sdk.cloud.models

data class IncentivePlan(val id: String,
                         val planName: String,
                         val startDate: String,
                         val endDate: String,
                         val planDetails: List<PlanDetail>
) {
    data class PlanDetail(val eventType: String,
                          val eventSubType: String,
                          val ruleType: String, val formula: Formula)

    data class Formula(val formulaType: String, val firstOperand: Int)
}
