package com.hercufy.models;

/**
 * Como se reparten los dias de una rutina. Hoy solo existe WEEKDAY (dias fijos de la
 * semana); ROTATION (tipo A/B/C, sin depender del dia) se anadira mas adelante sin
 * tener que migrar los datos existentes.
 */
public enum ScheduleType {
    WEEKDAY
}
