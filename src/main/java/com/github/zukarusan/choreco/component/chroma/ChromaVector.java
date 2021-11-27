package com.github.zukarusan.choreco.component.chroma;

import com.github.zukarusan.choreco.component.LogFrequency;
import com.github.zukarusan.choreco.component.LogFrequencyVector;
import com.github.zukarusan.choreco.util.PlotManager;
import lombok.Getter;

public abstract class ChromaVector {

    @Getter
    protected final float[] power;

    public ChromaVector(LogFrequencyVector logVector) {
        this.power = new float[Chroma.CHROMATIC_LENGTH];
    }

    public ChromaVector(float[] power) {
        if (power.length != Chroma.CHROMATIC_LENGTH)
            throw new IllegalCallerException("Must be chroma vector");
        this.power = power;
    }


    protected void mapPitch(float[] pitches) {
        assert pitches.length == LogFrequency.PITCH_LENGTH;
        for (int i = 0; i < LogFrequency.PITCH_LENGTH; i++) {
            this.power[i % 12] += pitches[i];
        }
    }

    public void plot() {
        PlotManager plotManager = PlotManager.getInstance();
        plotManager.createPlot("ChromaVector", "Chroma", power);
    }

}
