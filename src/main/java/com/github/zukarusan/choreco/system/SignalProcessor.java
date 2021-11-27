package com.github.zukarusan.choreco.system;

import com.github.zukarusan.choreco.component.spectrum.FrequencySpectrum;
import com.github.zukarusan.choreco.component.SignalFFT;
import com.github.zukarusan.choreco.component.Signal;
import com.github.zukarusan.choreco.component.spectrum.Spectrum;
import com.github.zukarusan.choreco.system.exception.STFTException;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.util.ArrayList;
import java.util.List;


public class SignalProcessor {

    public static int[] peakDetection(float[] data, float threshold) {

        // init stats instance
        SummaryStatistics stats = new SummaryStatistics();

        // the results (peaks, 1 or -1) of our algorithm
        List<Integer> peaks = new ArrayList<>();

        // init average and standard deviation
        for (float datum : data) {
            stats.addValue(datum);
        }
        float avg = (float) stats.getMean();
        float std = (float) Math.sqrt(stats.getPopulationVariance()); // getStandardDeviation() uses sample variance
        stats.clear();

        // loop input
        for (int i = 0; i < data.length; i++) {

            // if the distance between the current value and average is enough standard deviations (threshold) away
            if (Math.abs(data[i] - avg) > threshold * std) {

                peaks.add(i);
                //peaks[i] = 1;
                // this is a signal (i.e. peak), determine if it is a positive or negative signal
//                if (data[i] > avg) {
//                    peaks[i]    = 1;
//                } else {
//                    peaks[i] = -1;
//                }

            }
//            else {
//                // ensure this signal remains a zero
//                peaks[i] = 0;
//            }
        }

        return peaks.stream().mapToInt(i -> i).toArray();
    }

    public static int freqToIdx(float frequency, float freq_res) {
        return (int) (frequency / freq_res);
    }

    public static int[] freqToIdx(float[] frequencies, float freq_res) {
        int[] indexes = new int[frequencies.length];

        for (int i = 0; i < frequencies.length; i++) {
            indexes[i] = (int) (frequencies[i] / freq_res);
        }
        return indexes;
    }

    public static float idxToFreq(int index, float freq_res) {
        return freq_res * index;
    }

    public static float[] idxToFreq(int[] indexes, float freq_res) {
        float[] frequencies = new float[indexes.length];

        for (int i = 0; i < indexes.length; i++) {
            frequencies[i] = freq_res * indexes[i];
        }
        return frequencies;
    }


    // data input in frequency-domain with amplitudes (not in complex number representation)
    public static FrequencySpectrum trimOfRange(FrequencySpectrum spectrum, float from_freq, float to_freq) {
        float freq_res = spectrum.getFrequencyResolution();
        float[][] trimmed = trimOfRange(spectrum.getDataBuffer(), from_freq, to_freq, freq_res);
        return new FrequencySpectrum(
                "trimmed"+from_freq+"-"+to_freq+"_"+spectrum.getName(),
                trimmed,
                spectrum.getSampleRate(),
                spectrum.getFrequencyResolution(),
                ((int) (from_freq / freq_res)) * freq_res);
    }

    public static float[][] trimOfRange(float[][] fft_amp_data, float from_freq, float to_freq, float freq_res) {
        int idx_from = freqToIdx(from_freq, freq_res);
        int idx_to = freqToIdx(to_freq, freq_res);
        int length = idx_to-idx_from+1;
        float[][] trimmed = new float[fft_amp_data.length][length];
        for (int i = 0; i < trimmed.length; i ++) {
            System.arraycopy(
                    fft_amp_data[i], idx_from,
                    trimmed[i], 0,
                    length
            );
        }
        return trimmed;
    }

    public static Signal trimOfRange(Signal signal, float from_freq, float to_freq) throws STFTException {
        if (signal.getDomain() != Signal.Domain.FREQUENCY_DOMAIN) {
            throw new STFTException("SIGNAL_DOMAIN_MISMATCH", "Signal must be in Frequency-domain", new IllegalArgumentException("fft_amp_data not match"));
        }
        float freq_res = signal.getFrequencyResolution();
        float[] trimmed = trimOfRange(signal.getData(), from_freq, to_freq, freq_res);
        if (signal instanceof SignalFFT)
            return new SignalFFT(
                    "trimmed"+from_freq+"-"+to_freq+"_"+signal.getName(),
                    trimmed,
                    signal.getSampleRate(),
                    signal.getFrequencyResolution(),
                    ((SignalFFT) signal).isNyquist(),
                    ((int) (from_freq / freq_res)) * freq_res
            );
        else return new Signal(
                "trimmed"+from_freq+"-"+to_freq+"_"+signal.getName(),
                trimmed,
                signal.getSampleRate(),
                Signal.Domain.FREQUENCY_DOMAIN
        );
    }

    public static float[] trimOfRange(float[] fft_amp_data, float from_freq, float to_freq, float freq_res){
        int idx_from = (int) (from_freq / freq_res);
        int idx_to = (int) (to_freq / freq_res);
        int length = idx_to-idx_from+1;
        float[] trimmed = new float[length];
        System.arraycopy(
                fft_amp_data, idx_from,
                trimmed, 0,
                length
        );
        return trimmed;
    }


    public static void normalizeEuclid(float[][] data) {
//        float[][]
    }

    public static void normalizeZeroOne(Spectrum spectrum) {
        normalizeZeroOne(spectrum.getDataBuffer());
    }

    public static void normalizeZeroOne(float[][] data) {
        int frameTotal = data.length;
        int n = data[0].length;
        float maxAmp = data[0][0], minAmp = maxAmp;
        for (float[] datum : data) {
            if (datum[0] > maxAmp) {
                maxAmp = datum[0];
            } else if (datum[0] < minAmp) {
                minAmp = datum[0];
            }
            for (int j = 0; j + 1 < n; j++) {
                if (datum[j + 1] > maxAmp) {
                    maxAmp = datum[j + 1];
                } else if (datum[j + 1] < minAmp) {
                    minAmp = datum[j + 1];
                }
            }
        }
        float diff = maxAmp - minAmp;
        for (int i = 0; i < frameTotal; i++){
            for (int j = 0; j < n; j++){
                data[i][j] = (data[i][j]-minAmp)/diff;
            }
        }
    }

    public static void normalizeZeroOne(Signal signal) {
        normalizeZeroOne(signal.getData());
    }

    public static void normalizeZeroOne(float[] data) {
        float maxAmp = data[0], minAmp = maxAmp;

        for (int i = 0; i + 1 < data.length; i++) {
            if (data[i + 1] > maxAmp) {
                maxAmp = data[i + 1];
            } else if (data[i + 1] < minAmp) {
                minAmp = data[i + 1];
            }
        }
        float diff = maxAmp - minAmp;
        for (int i = 0; i < data.length; i++){
            data[i] = (data[i]-minAmp)/diff;
        }
    }

    public static void powerToDb(Spectrum spectrum) {
        powerToDb(spectrum.getDataBuffer());
    }

    public static void powerToDb(float[][] data) {
        int frameTotal = data.length;
        int n = data[0].length;
        for (int i = 0; i < frameTotal; i++) {
            for (int j = 0; j < n; j++) {
                if (data[i][j] != 0) {
                    data[i][j] = (float) (10 * Math.log10(data[i][j]));
                }
            }
        }
    }

    public static void powerToDb(Signal signal) {
        powerToDb(signal.getData());
    }


    public static void powerToDb(float[] data) {
        for (int i = 0; i < data.length; i++) {
            data[i] = (float) (10 * Math.log10(data[i]));
        }
    }

}
