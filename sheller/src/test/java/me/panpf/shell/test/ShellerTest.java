package me.panpf.shell.test;

import android.support.annotation.Nullable;

import org.junit.Assert;
import org.junit.Test;

import me.panpf.shell.Command;
import me.panpf.shell.CommandResult;
import me.panpf.shell.ConditionalCommand;
import me.panpf.shell.Sheller;
import me.panpf.shell.SuspendCommand;

public class ShellerTest {
    @Test
    public void testOnce() {
        Sheller sheller = new Sheller("ls");
        CommandResult result = sheller.syncExecute();
        Assert.assertTrue(result.isSuccess());
    }

    @Test
    public void testOnceFailed() {
        Sheller sheller = new Sheller("lss");
        CommandResult result = sheller.syncExecute();
        Assert.assertFalse(result.isSuccess());
    }

    @Test
    public void testMulti() {
        Sheller sheller = new Sheller("ls", "which mkdir");
        CommandResult result = sheller.syncExecute();
        Assert.assertTrue(result.isSuccess());
    }

    @Test
    public void testMultiFailed() {
        Sheller sheller = new Sheller("ls", "whichh mkdir");
        CommandResult result = sheller.syncExecute();
        Assert.assertFalse(result.isSuccess());
    }

    @Test
    public void testConditionalCommand() {
        Command lsCommand = new Command("ls");
        Command errorLsCommand = new Command("lss");
        ConditionalCommand whichCommand = new ConditionalCommand("whichh mkdir") {
            @Override
            public boolean checkLastResult(@Nullable CommandResult lastResult) {
                return lastResult != null && lastResult.isSuccess();
            }
        };
        Command pwdCommand = new Command("pwd");

        Assert.assertTrue(new Sheller(lsCommand, whichCommand).syncExecute().getCommand() == whichCommand);

        Assert.assertTrue(new Sheller(errorLsCommand, whichCommand).syncExecute().getCommand() == errorLsCommand);

        Assert.assertTrue(new Sheller(lsCommand, whichCommand, pwdCommand).syncExecute().getCommand() == pwdCommand);
    }

    @Test
    public void testSuspendCommand() {
        Command lsCommand = new Command("ls");
        Command errorLsCommand = new Command("lss");
        SuspendCommand whichCommand = new SuspendCommand("whichh mkdir") {
            @Override
            public boolean checkLastResult(@Nullable CommandResult lastResult) {
                return lastResult != null && lastResult.isSuccess();
            }
        };
        Command pwdCommand = new Command("pwd");

        Assert.assertTrue(new Sheller(lsCommand, whichCommand).syncExecute().getCommand() == whichCommand);

        Assert.assertTrue(new Sheller(errorLsCommand, whichCommand).syncExecute().getCommand() == errorLsCommand);

        Assert.assertTrue(new Sheller(errorLsCommand, whichCommand, pwdCommand).syncExecute().getCommand() == errorLsCommand);
    }
}
