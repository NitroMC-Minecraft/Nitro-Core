package de.grimlock.nitromc.event;

public class AsyncDataIntegrityCheckEvent {
    private final String key;
    private final Object value;
    private boolean valid = true;

    public AsyncDataIntegrityCheckEvent(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() { return key; }
    public Object getValue() { return value; }
    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
}
