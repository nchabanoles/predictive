package predictive.data;

import java.security.SecureRandom;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Nicolas Chabanoles on 05/05/17.
 */
public class Sampler implements Collector<Long, List<Long>, List<Long>> {

    final Random rand = new SecureRandom();
    final List<Long> omittedValues = new ArrayList<>();
    final int size;
    int c = 0;

    public Sampler(int size) {
        this.size = size;
    }

    private void addIt(final List<Long> in, Long s) {
        if (in.size() < size) {
            // ensure to have at least size elements
            in.add(s);
        }
        else {
            int replaceInIndex = (int) (rand.nextDouble() * (size + (c++) + 1));
            Long omitted = s;
            if (replaceInIndex < size) {
                omitted = in.set(replaceInIndex, s);
            }
            omittedValues.add(omitted);
        }
    }

    @Override
    public Supplier<List<Long>> supplier() {
        return ArrayList::new;
    }

    @Override
    public BiConsumer<List<Long>, Long> accumulator() {
        return this::addIt;
    }

    @Override
    public BinaryOperator<List<Long>> combiner() {
        return (left, right) -> {
            left.addAll(right);
            return left;
        };
    }

    @Override
    public Set<Characteristics> characteristics() {
        return EnumSet.of(Collector.Characteristics.UNORDERED, Collector.Characteristics.IDENTITY_FINISH);
    }

    @Override
    public Function<List<Long>, List<Long>> finisher() {
        return (i) -> i;
    }

    public List<Long> listOmittedValues() {
        return omittedValues;
    }
}