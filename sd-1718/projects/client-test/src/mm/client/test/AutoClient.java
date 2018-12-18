/* -------------------------------------------------------------------------- */

package mm.client.test;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import mm.client.common.net.NetClient;
import mm.common.data.Hero;
import mm.common.data.LoginError;
import mm.common.data.RandomHero;

/* -------------------------------------------------------------------------- */

/**
 * Automated client designed for testing.
 *
 * @author Alberto Faria
 * @author Fábio Fontes
 */
public class AutoClient
{
    private final int id;

    private final NetClient netClient;
    private final InetSocketAddress serverEndpoint;

    private final DurationRange joinMatchmakingDelay;
    private final DurationRange acceptMatchDelay;
    private final DurationRange selectHeroDelay;

    private final Consumer< Throwable > onError;

    private Timer timer;

    private boolean ignoreErrors;

    /* ---------------------------------------------------------------------- */

    private void onDisconnect(Throwable cause)
    {
        timer.cancel();

        synchronized (this)
        {
            if (ignoreErrors)
                return;

            ignoreErrors = true;
        }

        if (cause != null)
            onError.accept(cause);
    }

    // Causes this AutoClient to stop with the specified cause.
    private void fail(Throwable cause)
    {
        synchronized (this)
        {
            if (ignoreErrors)
                return;

            ignoreErrors = true;
            timer.cancel();
        }

        netClient.disconnect();

        onError.accept(cause);
    }

    private void schedule(
        DurationRange delayRange, boolean repeat,
        Runnable task
        )
    {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run()
            {
                try
                {
                    task.run();
                }
                catch (Throwable t)
                {
                    fail(t);
                }
            }
        };

        long period = delayRange.getRandUniformAsMilliseconds();

        synchronized (this)
        {
            timer.cancel();
            timer = new Timer();

            if (repeat)
                timer.schedule(timerTask, period, period);
            else
                timer.schedule(timerTask, period);
        }
    }


    private void connect()
    {
        // set callbacks

        netClient.clearCallbacks();
        netClient.setOnDisconnect(this::onDisconnect);

        netClient.setOnConnectSucceded(() -> {
            login();
        });

        netClient.setOnConnectFailed(cause -> {
            fail(cause);
        });

        // connect

        netClient.connect(serverEndpoint);
    }

    private void login()
    {
        String username = "autoclient" + id;
        String password = "123456";

        // set callbacks

        netClient.clearCallbacks();
        netClient.setOnDisconnect(this::onDisconnect);

        netClient.setOnLoginSucceded(() -> {
            joinMatchmaking();
        });

        netClient.setOnLoginFailed(error -> {
            if (error == LoginError.USERNAME_DOESNT_EXIST)
                netClient.signUp(username, password);
            else
                fail(new RuntimeException("login failed with error " + error));
        });

        netClient.setOnSignUpFailed(error -> {
            fail(new RuntimeException("sign up failed with error " + error));
        });

        // login

        netClient.login(username, password);
    }

    private void joinMatchmaking()
    {
        // set callbacks

        netClient.clearCallbacks();
        netClient.setOnDisconnect(this::onDisconnect);

        netClient.setOnJoinedMatchmaking(() -> {
            awaitMatch();
        });

        // schedule join matchmaking

        schedule(joinMatchmakingDelay, false, () -> {
            netClient.joinMatchmaking();
        });
    }

    private void awaitMatch()
    {
        // set callbacks

        netClient.clearCallbacks();
        netClient.setOnDisconnect(this::onDisconnect);

        netClient.setOnLeftMatchmaking(cause -> {
            fail(new RuntimeException("left matchmaking with cause " + cause));
        });

        netClient.setOnMatchFound(time -> {
            acceptMatch();
        });
    }

    private void acceptMatch()
    {
        // set callbacks

        netClient.clearCallbacks();
        netClient.setOnDisconnect(this::onDisconnect);

        netClient.setOnMatchCanceled(() -> {
            timer.cancel();
            awaitMatch();
        });

        netClient.setOnLeftMatchmaking(cause -> {
            timer.cancel();
            fail(new RuntimeException("left matchmaking with cause " + cause));
        });

        netClient.setOnJoinedLobby(time -> {
            selectHero();
        });

        // schedule accept match

        schedule(acceptMatchDelay, false, () -> {
            try
            {
                netClient.acceptMatch();
            }
            catch (Throwable t)
            {
            }
        });
    }

    private void selectHero()
    {
        // set callbacks

        netClient.clearCallbacks();
        netClient.setOnDisconnect(this::onDisconnect);

        netClient.setOnLobbyDied(cause -> {
            timer.cancel();
            joinMatchmaking();
        });

        netClient.setOnMatchPlayed((matchSummary, oldRank, newRank) -> {
            timer.cancel();
            joinMatchmaking();
        });

        // select hero

        if (selectHeroDelay.getMin() == 0 &
            selectHeroDelay.getMax() == 0)
        {
            netClient.selectHero(new RandomHero());
        }
        else
        {
            schedule(selectHeroDelay, true, () -> {
                try
                {
                    List< Hero > h = netClient.getUnselectedHeroes();

                    netClient.selectHero(h.get(
                        ThreadLocalRandom.current().nextInt(h.size())
                        ));
                }
                catch (Throwable t)
                {
                }
            });
        }
    }

    /* ---------------------------------------------------------------------- */

    /**
     * Creates a new instance of AutoClient.
     *
     * @param id the identifier for the client
     * @param serverEndpoint the server's endpoint
     * @param joinMatchmakingDelay delay range for joining matchmaking
     * @param acceptMatchDelay delay range for accepting a match
     * @param selectHeroDelay delay range for selecting a hero
     * @param onError callback to be run when the AutoClient encounters an error
     */
    public AutoClient(
        int id,
        InetSocketAddress serverEndpoint,
        DurationRange joinMatchmakingDelay,
        DurationRange acceptMatchDelay,
        DurationRange selectHeroDelay,
        Consumer< Throwable > onError
        )
    {
        this.id = id;

        this.netClient      = new NetClient();
        this.serverEndpoint = Objects.requireNonNull(serverEndpoint);

        this.joinMatchmakingDelay = Objects.requireNonNull(joinMatchmakingDelay);
        this.acceptMatchDelay     = Objects.requireNonNull(acceptMatchDelay);
        this.selectHeroDelay      = Objects.requireNonNull(selectHeroDelay);

        this.onError = Objects.requireNonNull(onError);

        this.timer = new Timer();

        ignoreErrors = false;
    }

    /* ---------------------------------------------------------------------- */

    /**
     * Starts this AutoClient.
     * <p>
     * May only be called once on a given instance of AutoClient.
     */
    public void start()
    {
        connect();
    }

    /**
     * Asynchronously stops this AutoClient.
     * <p>
     * Note that this method may return before the client is stopped.
     */
    public void stop()
    {
        ignoreErrors = true;

        netClient.disconnect();
    }

    /**
     * Waits until this AutoClient is stopped.
     * <p>
     * If this AutoClient is already stopped or not started, returns
     * immediately.
     */
    public void waitUntilStopped()
    {
        netClient.waitUntilDisconnected();
    }

}

/* -------------------------------------------------------------------------- */
