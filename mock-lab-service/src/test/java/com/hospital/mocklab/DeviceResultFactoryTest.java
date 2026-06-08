package com.hospital.mocklab;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeviceResultFactoryTest {

    private final DeviceResultFactory factory = new DeviceResultFactory();

    @Test
    void randomBatchProducesTubesEachWithAPanel() {
        List<SampleBatchDto> batch = factory.randomBatch(new Random(42));

        assertThat(batch).hasSizeBetween(1, 2);
        for (SampleBatchDto tube : batch) {
            assertThat(tube.sampleId()).isNotBlank();
            assertThat(tube.patientId()).isNotBlank();
            assertThat(tube.measuredAt()).isNotNull();
            assertThat(tube.tests()).hasSizeBetween(3, 6);
            // All tests in a tube are distinct test codes (a real panel has no repeats).
            assertThat(tube.tests().stream().map(TestResultDto::testCode).distinct().count())
                    .isEqualTo(tube.tests().size());
        }
    }

    @Test
    void sampleIdsAreUniqueAcrossARandomBatch() {
        List<SampleBatchDto> batch = factory.randomBatch(new Random(7));
        long distinct = batch.stream().map(SampleBatchDto::sampleId).distinct().count();
        assertThat(distinct).isEqualTo(batch.size());
    }

    @Test
    void randomSampleIdsRemainUniqueAcrossServiceRestarts() {
        DeviceResultFactory firstRun = new DeviceResultFactory("run-a");
        DeviceResultFactory secondRun = new DeviceResultFactory("run-b");

        String firstId = firstRun.randomBatch(new Random(7)).get(0).sampleId();
        String secondId = secondRun.randomBatch(new Random(7)).get(0).sampleId();

        assertThat(firstId).isNotEqualTo(secondId);
    }

    @Test
    void duplicateScenarioRepeatsTheSameSampleId() {
        List<SampleBatchDto> batch = factory.batchFor(Scenario.DUPLICATE);
        assertThat(batch).hasSize(2);
        assertThat(batch.get(0).sampleId()).isEqualTo(batch.get(1).sampleId());
    }

    @Test
    void missingFieldScenarioHasATestWithNullValue() {
        SampleBatchDto tube = factory.batchFor(Scenario.MISSING_FIELD).get(0);
        assertThat(tube.tests()).anyMatch(t -> t.value() == null);
    }

    @Test
    void invalidUnitScenarioHasABadUnit() {
        SampleBatchDto tube = factory.batchFor(Scenario.INVALID_UNIT).get(0);
        assertThat(tube.tests()).anyMatch(t -> !"mg/dL".equals(t.unit()));
    }

    @Test
    void deviceErrorScenarioThrows() {
        assertThatThrownBy(() -> factory.batchFor(Scenario.DEVICE_ERROR))
                .isInstanceOf(DeviceOfflineException.class);
    }
}
