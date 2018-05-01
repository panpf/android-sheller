package me.panpf.shell.test;

import android.support.annotation.Nullable;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import me.panpf.shell.Cmd;
import me.panpf.shell.CmdResult;
import me.panpf.shell.ConditionalCmd;
import me.panpf.shell.Sheller;
import me.panpf.shell.SuspendCmd;

@RunWith(AndroidJUnit4.class)
public class ShellerTest {
    @Test
    public void testOnce() {
        Sheller sheller = new Sheller("ls");
        CmdResult result = sheller.syncExecute();
        Assert.assertTrue(result.isSuccess());
    }

    @Test
    public void testOnceFailed() {
        Sheller sheller = new Sheller("lss");
        CmdResult result = sheller.syncExecute();
        Assert.assertFalse(result.isSuccess());
    }

    @Test
    public void testMulti() {
        Sheller sheller = new Sheller("ls", "which mkdir");
        CmdResult result = sheller.syncExecute();
        Assert.assertTrue(result.isSuccess());
    }

    @Test
    public void testMultiFailed() {
        Sheller sheller = new Sheller("ls", "whichh mkdir");
        CmdResult result = sheller.syncExecute();
        Assert.assertFalse(result.isSuccess());
    }

    @Test
    public void testConditionalCmd() {
        Cmd lsCmd = new Cmd("ls");
        Cmd errorLsCmd = new Cmd("lss");
        ConditionalCmd whichCmd = new ConditionalCmd("whichh mkdir") {
            @Override
            public boolean checkLastResult(@Nullable CmdResult lastResult) {
                return lastResult != null && lastResult.isSuccess();
            }
        };
        Cmd pwdCmd = new Cmd("pwd");

        Assert.assertTrue(new Sheller(lsCmd, whichCmd).syncExecute().getCmd() == whichCmd);

        Assert.assertTrue(new Sheller(errorLsCmd, whichCmd).syncExecute().getCmd() == errorLsCmd);

        Assert.assertTrue(new Sheller(lsCmd, whichCmd, pwdCmd).syncExecute().getCmd() == pwdCmd);
    }

    @Test
    public void testSuspendCmd() {
        Cmd lsCmd = new Cmd("ls");
        Cmd errorLsCmd = new Cmd("lss");
        SuspendCmd whichCmd = new SuspendCmd("whichh mkdir") {
            @Override
            public boolean checkLastResult(@Nullable CmdResult lastResult) {
                return lastResult != null && lastResult.isSuccess();
            }
        };
        Cmd pwdCmd = new Cmd("pwd");

        Assert.assertTrue(new Sheller(lsCmd, whichCmd).syncExecute().getCmd() == whichCmd);

        Assert.assertTrue(new Sheller(errorLsCmd, whichCmd).syncExecute().getCmd() == errorLsCmd);

        Assert.assertTrue(new Sheller(errorLsCmd, whichCmd, pwdCmd).syncExecute().getCmd() == errorLsCmd);
    }
}
