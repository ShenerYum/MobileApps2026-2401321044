package com.budgeteer.app.domain.exception

sealed class DomainException(message: String) : RuntimeException(message)

class InvalidAmountException(message: String) : DomainException(message)
class InvalidCurrencyCodeException(message: String) : DomainException(message)
class CurrencyMismatchException(message: String) : DomainException(message)
class InvalidExchangeRateException(message: String) : DomainException(message)
class CategoryTypeMismatchException(message: String) : DomainException(message)
class InvalidBudgetException(message: String) : DomainException(message)
class PeriodMismatchException(message: String) : DomainException(message)
