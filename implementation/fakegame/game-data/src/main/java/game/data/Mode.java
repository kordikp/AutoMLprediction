package game.data;

/**
 * In ACTIVE mode, a request to produce output is propagated to inputs
 * In PASSIVE model, a cached output is provided (PASSIVE mode is used during learning)
 */
public enum Mode {
    ACTIVE, PASSIVE
}
