package vw;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * By making this class final, the JIT compiler should be able to hotspot this class as necessary.
 * Created by jmorra on 11/23/14.
 */
public final class VW implements Closeable {
    static {
        try {
            NativeUtils.loadOSDependentLibrary("/vw_jni", ".lib");
        }
        catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final AtomicBoolean isOpen;
    private final long nativePointer;

    public VW(String command) {
        isOpen = new AtomicBoolean(true);
        nativePointer = initialize(command);
    }

    /**
     * Runs prediction on <code>example</code> and returns the prediction output.  Note that
     * this only works for "simple" VW predictions.
     *
     * @param example a single vw example string
     * @return prediction output
     */
    public float predict(String example) {
        if (isOpen.get()) {
            return predict(example, false, nativePointer);
        }
        throw new IllegalStateException("Already closed.");
    }

    /**
     * Run prediction on <code>examples</code> and returns all of the predictions.  Note that
     * this only works for "simple" VW predictions.
     * @param examples an array of vw example strings
     * @return predictions
     */
    public float[] predict(String[] examples) {
        if (isOpen.get()) {
            return batchPredict(examples, false, nativePointer);
        }
        throw new IllegalStateException("Already closed.");
    }

    /**
     * Run prediction on <code>examples</code> and returns all of the predictions.  Note that
     * this only works for "simple" VW predictions.
     * @param examples a list of vw example strings
     * @return predictions
     */
    public List<Float> predict(List<String> examples) {
        return convertToList(predict(examples.toArray(new String[examples.size()])));
    }

    /**
     * Runs learning on <code>example</code> and returns the prediction output.  Note that
     * this only works for "simple" VW predictions.
     *
     * @param example a single vw example string
     * @return prediction output
     */
    public float learn(String example) {
        if (isOpen.get()) {
            return predict(example, true, nativePointer);
        }
        throw new IllegalStateException("Already closed.");
    }

    /**
     * Run learning on <code>examples</code> and returns all of the predictions.  Note that
     * this only works for "simple" VW predictions.
     * @param examples an array of vw example strings
     * @return predictions
     */
    public float[] learn(String[] examples) {
        if (isOpen.get()) {
            return batchPredict(examples, true, nativePointer);
        }
        throw new IllegalStateException("Already closed.");
    }

    /**
     * Run prediction on <code>examples</code> and returns all of the predictions.  Note that
     * this only works for "simple" VW predictions.
     * @param examples a list of vw example strings
     * @return predictions
     */
    public List<Float> learn(List<String> examples) {
        return convertToList(learn(examples.toArray(new String[examples.size()])));
    }

    /**
     * Close the VW instance.  This MUST be called in order to free up the native memory.
     * After this is called no future calls to this object are permitted.
     */
    public void close() {
        if (isOpen.getAndSet(false)) {
            closeInstance(nativePointer);
        }
    }

    private List<Float> convertToList(float[] data) {
        List<Float> out = new ArrayList<Float>(data.length);
        for (float aData : data) {
            out.add(aData);
        }
        return out;
    }

    private native long initialize(String command);
    private native float predict(String example, boolean learn, long nativePointer);
    private native float[] batchPredict(String[] examples, boolean learn, long nativePointer);
    private native void closeInstance(long nativePointer);
}
