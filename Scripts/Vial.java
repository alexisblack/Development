import org.powerbot.script.Condition;
import org.powerbot.script.PollingScript;
import org.powerbot.script.Script;
import org.powerbot.script.rt6.*;

import java.util.concurrent.Callable;

@Script.Manifest(name="Vial", description="Fills Your Vials At The Grand Exchange.")
public class Vial extends PollingScript<ClientContext> {
    private static final int fountainId = 47150;
    private static final  int vialId = 229;
    private static final int fvialId = 227;
    final Component fill = ctx.widgets.component(1370, 37);
    final GameObject fountain = ctx.objects.select().id(fountainId).nearest().poll();
    final Item v = ctx.backpack.select().id(vialId).first().poll();

    @Override
    public void poll() {
        switch (state()) {
            //FILL == Pain In My Ass
            case FILL:
                if (fountain.inViewport() && !ctx.players.local().inMotion()) {
                        if (!ctx.hud.opened(Hud.Window.BACKPACK)) {
                            ctx.hud.open(Hud.Window.BACKPACK);
                        }
                    if (!ctx.backpack.itemSelected()) {
                        v.interact("Use");
                        fountain.interact("Use", "Fountain");
                    }
                            Condition.wait(new Callable<Boolean>() {
                                @Override
                                public Boolean call() throws Exception {
                                    return fill.visible();
                                }
                            }, 500, 10);
                            fill.click(true);
                            Condition.wait(new Callable<Boolean>() {
                                @Override
                                public Boolean call() {
                                    return ctx.backpack.select().id(vialId).count() == 0;
                                }
                            }, 1000, 20);
                    }else {
                        if (!ctx.players.local().inMotion()) {
                            ctx.movement.step(fountain);
                        }
                            ctx.camera.turnTo(fountain);
                    }
                break;
            case BANK:
                if (ctx.players.local().animation() == -1) {
                    if (ctx.bank.inViewport() && !ctx.players.local().inMotion()) {
                        ctx.bank.open();
                        }else {
                        if (!ctx.players.local().inMotion()) {
                            ctx.movement.step(ctx.bank.nearest());
                        }
                        ctx.camera.turnTo(ctx.bank.nearest());
                    }
                    if (ctx.bank.opened()) {
                        ctx.bank.depositInventory();
                    }
                }
                break;
            case Withdrawal:
                if (ctx.players.local().animation() == -1) {
                    if (ctx.bank.inViewport()) {
                        ctx.bank.open();
                    }else {
                         ctx.movement.step(ctx.bank.nearest());
                        ctx.camera.turnTo(ctx.bank.nearest());
                    }
                if (ctx.bank.opened()) {
                    if (ctx.backpack.select().count() >= 1) {
                        ctx.bank.depositInventory();
                        }else {
                        ctx.bank.withdraw(vialId, 28);
                        if (ctx.backpack.select().id(vialId).count() >= 1) {
                            ctx.bank.close();
                            }
                        }
                    }
                }
                break;
            }
        }
    private State state() {
        if (ctx.backpack.select().id(vialId).count() >= 1) {
            return State.FILL;
        }
        if (ctx.backpack.select().id(vialId).count() == 0 && ctx.backpack.select().id(fvialId).count() >= 1) {
            return State.BANK;
        }
        if (ctx.backpack.select().id(vialId).count() == 0 && ctx.backpack.select().id(fvialId).count() == 0) {
            return State.Withdrawal;
        }
        return State.FILL;
    }
    private enum State {
        FILL, BANK, Withdrawal
    }

}