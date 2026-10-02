package com.hercufy.services;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/** Forma de cada elemento de data/exercise-translations-es.json: {"id": "...", "nameEs": "..."}. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class ExerciseTranslationImportRecord {
    private String id;
    private String nameEs;
}
