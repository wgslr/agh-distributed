#!/usr/bin/env python3

import sys
import os
import Ice
from typing import Optional

Ice.loadSlice('../api/bank.ice')
import bank


class Account:
    def __init__(self, proxy, pesel, key, is_premium):
        self.proxy = proxy
        self.pesel = pesel
        self.key = key
        self.is_premium = is_premium

    def getBalance(self):
        return self.proxy.getBalance({'key': self.key})

    def requestLoan(self, *args):
        return self.proxy.requestLoan(*args, {'key': self.key})


DEFAULT_PORT = 10000
BASE_CURRENCY = bank.Currency.PLN

CURRENCY = {
    'PLN': bank.Currency.PLN,
    'EUR': bank.Currency.EUR,
    'GBP': bank.Currency.GBP,
    'USD': bank.Currency.USD,
    'HRK': bank.Currency.HRK,
}

port = DEFAULT_PORT
current_account: Optional[Account] = None


def printHelp():
    print("Available commands:")
    print("help")
    print("register <first name> <last name> <pesel> <monthly income>")
    print("login <category> <pesel> <key>")
    print("balance")
    print("loan <value> <currency> <period in months>")
    print("exit")


def getProxy(communicator, category, name):
    return communicator.stringToProxy('{}/{}:tcp -h localhost -p {port}:udp -h localhost -p {port}'.format(
                                      category, name, port=port))


def getAccount(communicator, category, pesel):
    proxy = getProxy(communicator, category, pesel)
    premium = bank.PremiumAccountPrx.checkedCast(proxy)
    if premium:
        return premium
    else:
        return bank.AccountPrx.checkedCast(proxy)


def ensureArgsLen(args, required):
    if len(args) != required:
        raise ValueError(
            "Expected {} arguments, {} given!".format(required, len(args)))


def formatBalance(balance):
    return "{:.2f} {}".format(balance.minorUnitAmount / 100, balance.currency)


def formatPrompt():
    global current_account
    if current_account is None:
        return "not logged in>"
    else:
        return "{pesel}>".format(pesel=current_account.pesel)




def handleRegister(communicator, args):
    global current_account
    ensureArgsLen(args, 4)

    [fname, lname, pesel, income_str] = args
    income = float(income_str)

    factory_prx = getProxy(communicator, "accountfactory", "accountfactory")
    factory = bank.AccountFactoryPrx.checkedCast(factory_prx)

    income_obj = bank.MoneyAmount(income * 100, BASE_CURRENCY)
    try:
        result = factory.createAccount(fname, lname, pesel, income_obj)
        category = "premium" if result.isPremium else "standard"

        if result.isPremium:
            account_proxy = bank.PremiumAccountPrx.checkedCast(result.account)
        else:
            account_proxy = bank.AccountPrx.checkedCast(result.account)

        current_account = Account(
            account_proxy, pesel, result.key, result.isPremium)

        print("Registered a {} account. Access key: {}".format(category, result.key))
    except bank.AccountExistsException:
        print("Account using this PESEL is already registered!")


def handleLogin(communicator, args):
    try:
        global current_account
        ensureArgsLen(args, 3)
        if args[0].lower() == "standard":
            category = "standard"
        elif args[0].lower() == "premium":
            category = "premium"
        else:
            raise ValueError("Category must be 'standard' or 'premium'")

        [pesel, key] = args[1:]

        account = getAccount(communicator, category, pesel)
        balance = account.getBalance({'key': key})

        print("Logged in. Current balance: " + formatBalance(balance))
        current_account = Account(
            account, pesel, key, category.lower() == 'premium')
    except Ice.ObjectNotExistException:
        print("Given account does not exist!")


def handleBalance(communicator, args):
    if current_account is None:
        print("You must log in to check balance!")
        return
    balance = current_account.getBalance()
    print("Current balance: " + formatBalance(balance))


def handleLoan(communicator, args):
    if current_account is None:
        print("You must log in to request a loan!")
        return

    ensureArgsLen(args, 3)
    [value, currency_str, duration] = args

    print(current_account.is_premium)
    print(current_account.key)
    if not current_account.is_premium:
        print("Standard account cannot request a loan!")
        return

    if currency_str.upper() in CURRENCY:
        currency = CURRENCY[currency_str.upper()]
    else:
        raise ValueError("Unknown currency!")

    value_obj = bank.MoneyAmount(int(float(value) * 100), CURRENCY[currency_str.upper()])
    result = current_account.requestLoan(value_obj, int(duration))

    print("Loan cost: {:.2f} {} ({:.2f} {})".format(
        result.foreignCost.minorUnitAmount/ 100, result.foreignCost.currency,
        result.convertedCost.minorUnitAmount/ 100, result.convertedCost.currency))



def handleExit(communicator, _args):
    sys.exit()


actionToHandler = {
    'register': handleRegister,
    'login': handleLogin,
    'loan': handleLoan,
    'balance': handleBalance,
    'help': lambda _args: printHelp(),
    'exit': handleExit,
}


def repl_loop(communicator):
    printHelp()
    while True:
        print(formatPrompt(), end=' ')
        line = input().strip()
        words = line.split()
        if not words or words[0] not in actionToHandler:
            print("Unknown command!")
            printHelp()
            continue

        command, *args = words
        try:
            actionToHandler[command](communicator, args)
        except bank.AuthenticationException:
            print("Bad access key!")
        except bank.UnsupportedCurrencyException as e:
            print("This bank does not offer currency '{}'".format(e.currency))
        except Exception as error:
            print("Error: ", error)
    def getBalance(self):
        return self.proxy.getBalance({'key': self.key})

if __name__ == '__main__':
    try:
        port = int(sys.argv[1])
    except Exception as e:
        print("Defaulting to port number {}".format(DEFAULT_PORT))

    with Ice.initialize(sys.argv) as communicator:
        repl_loop(communicator)