package other;

import data.Instance;

/**
 * Created by lensenandr on 14/06/16.
 */
public interface DissimilarityMap {
    public double getDissim(Instance i1, Instance i2);

    double averageDissim();
}
