package it.unicam.cs.mpgc.rpg123465.model.floors.darkness;

/**
 * Stato indipendente dalla grafica della serratura a cifre.
 * Mantiene l'ordine di inserimento e non conosce né suoni né JavaFX.
 */
public final class CombinationLockState {

    private final int[] digits;
    private int activeSlot;

    public CombinationLockState(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("La serratura deve avere almeno una cifra.");
        }
        this.digits = new int[size];
    }

    public void reset() {
        for (int i = 0; i < digits.length; i++) {
            digits[i] = 0;
        }
        activeSlot = 0;
    }

    public int size() {
        return digits.length;
    }

    public int digitAt(int index) {
        return digits[index];
    }

    public int getActiveSlot() {
        return activeSlot;
    }

    public void increment() {
        digits[activeSlot] = (digits[activeSlot] + 1) % 10;
    }

    public void decrement() {
        digits[activeSlot] = (digits[activeSlot] + 9) % 10;
    }

    public boolean moveBack() {
        if (activeSlot == 0) {
            return false;
        }
        activeSlot--;
        return true;
    }

    /** @return {@code true} se è passata alla cifra successiva */
    public boolean confirm() {
        if (activeSlot == digits.length - 1) {
            return false;
        }
        activeSlot++;
        return true;
    }

    public String enteredCode() {
        StringBuilder code = new StringBuilder(digits.length);
        for (int digit : digits) {
            code.append(digit);
        }
        return code.toString();
    }
}
