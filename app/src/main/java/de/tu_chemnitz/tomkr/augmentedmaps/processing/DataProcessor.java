package de.tu_chemnitz.tomkr.augmentedmaps.processing;

import de.tu_chemnitz.tomkr.augmentedmaps.core.complextypes.InputType;
import de.tu_chemnitz.tomkr.augmentedmaps.core.complextypes.OutputType;

/**
 * Created by Tom Kretzschmar on 31.08.2017.
 *
 */

public interface DataProcessor {
    OutputType processData(InputType input);
}
