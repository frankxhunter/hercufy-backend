package com.hercufy.catalog;

import java.util.List;

/**
 * Agrupacion de musculos para los filtros de la busqueda de ejercicios. Es el mismo
 * reparto que usa el frontend (ver labels.ts / MUSCLE_GROUPS): mantenerlos iguales
 * evita que el filtro "Pecho" muestre cosas distintas segun si los datos vienen del
 * catalogo simulado o de esta API.
 */
public enum MuscleGroup {
    PECHO("pecho", "Pecho", List.of("chest")),
    ESPALDA("espalda", "Espalda", List.of("lats", "middle back", "lower back", "traps")),
    HOMBROS("hombros", "Hombros", List.of("shoulders")),
    BRAZOS("brazos", "Brazos", List.of("biceps", "triceps", "forearms")),
    PIERNAS("piernas", "Piernas", List.of("quadriceps", "hamstrings", "glutes", "calves", "adductors", "abductors")),
    CORE("core", "Core", List.of("abdominals"));

    private final String key;
    private final String label;
    private final List<String> muscles;

    MuscleGroup(String key, String label, List<String> muscles) {
        this.key = key;
        this.label = label;
        this.muscles = muscles;
    }

    public String key() {
        return key;
    }

    public String label() {
        return label;
    }

    public boolean matches(String muscle) {
        return muscle != null && muscles.contains(muscle);
    }

    public static MuscleGroup byKey(String key) {
        for (MuscleGroup g : values()) {
            if (g.key.equalsIgnoreCase(key)) {
                return g;
            }
        }
        return null;
    }

    public static MuscleGroup ofMuscle(String muscle) {
        for (MuscleGroup g : values()) {
            if (g.matches(muscle)) {
                return g;
            }
        }
        return null;
    }
}
