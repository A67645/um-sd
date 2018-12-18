/* -------------------------------------------------------------------------- */

package mm.server.auth;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import mm.common.data.LoginError;
import mm.common.data.SignUpError;
import mm.common.util.Validation;
import mm.server.error.LoginErrorException;
import mm.server.error.SignUpErrorException;

/* -------------------------------------------------------------------------- */

public class AccountManager
{
    private final Path accountFilePath;

    private final Map< String, Account > accounts;

    /* ---------------------------------------------------------------------- */

    public AccountManager()
    {
        this(null);
    }

    public AccountManager(Path accountFilePath)
    {
        this.accountFilePath = accountFilePath;

        this.accounts = new HashMap<>();

        if (accountFilePath != null && Files.exists(accountFilePath))
        {
            try
            {
                Files.lines(accountFilePath)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Account::fromString)
                    .forEachOrdered(a -> accounts.put(a.getUsername(), a));
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    /* ---------------------------------------------------------------------- */

    public synchronized int getNumAccounts()
    {
        return accounts.size();
    }

    public synchronized void saveToAccountFile()
    {
        if (accountFilePath != null)
        {
            Stream< String > lines =
                accounts
                .values()
                .stream()
                .map(Account::toString);

            try
            {
                Files.write(
                    accountFilePath,
                    (Iterable< String >)lines::iterator
                    );
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    /* ---------------------------------------------------------------------- */

    public synchronized Account login(String username, String password)
    {
        // validate credentials

        if (!Validation.isValidUsername(username))
            throw new LoginErrorException(LoginError.INVALID_USERNAME);

        if (!Validation.isValidPassword(password))
            throw new LoginErrorException(LoginError.INVALID_PASSWORD);

        // check if username exists

        Account account = accounts.get(username);

        if (account == null)
            throw new LoginErrorException(LoginError.USERNAME_DOESNT_EXIST);

        // check if password is correct

        if (!password.equals(account.getPassword()))
            throw new LoginErrorException(LoginError.WRONG_PASSWORD);

        // return account

        return account;
    }

    public synchronized Account register(String username, String password)
    {
        // validate credentials

        if (!Validation.isValidUsername(username))
            throw new SignUpErrorException(SignUpError.INVALID_USERNAME);

        if (!Validation.isValidPassword(password))
            throw new SignUpErrorException(SignUpError.INVALID_PASSWORD);

        // check if username is already in use

        if (accounts.containsKey(username))
            throw new SignUpErrorException(SignUpError.USERNAME_EXISTS);

        // add new account

        Account account = new Account(username, password);

        accounts.put(username, account);

        // return new account

        return account;
    }
}

/* -------------------------------------------------------------------------- */
