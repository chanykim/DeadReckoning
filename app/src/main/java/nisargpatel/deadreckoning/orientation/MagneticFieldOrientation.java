package nisargpatel.deadreckoning.orientation;

import org.ejml.simple.SimpleMatrix;

import nisargpatel.deadreckoning.extra.ExtraFunctions;

public final class MagneticFieldOrientation {

    private MagneticFieldOrientation() {}

    public static float[][] calcOrientation(float[] G_values, float[] M_values, float[] M_bias) {

        //G = Gyroscope, M = Magnetic Field
        //m = matrix
        //where r = roll, p = pitch, h = heading (yaw)

        //calculate roll and pitch from gravity
        double G_r = Math.atan2(G_values[1], G_values[2]);
        double G_p = Math.atan2(-G_values[0], G_values[1] * Math.sin(G_r) + G_values[2] * Math.cos(G_r));

        //create the rotation matrix representing the roll and pitch
        double[][] R_rp = {{Math.cos(G_p), Math.sin(G_p) * Math.sin(G_r), Math.sin(G_p) * Math.cos(G_r)},
                            {0, Math.cos(G_r), -Math.sin(G_r)},
                            {-Math.sin(G_p), Math.cos(G_p) * Math.sin(G_r), Math.cos(G_p) * Math.cos(G_r)}};

        //remove bias from magnetic field initial values
        double[][] M_init_unbiased = ExtraFunctions.vectorToMatrix(removeBias(M_values, M_bias));

        //convert arrays to matrices to allow for multiplication
        SimpleMatrix m_R_rp = new SimpleMatrix(R_rp);
        SimpleMatrix m_M_init_unbiased = new SimpleMatrix(M_init_unbiased);

        //rotate magnetic field values in accordance to gravity readings
        SimpleMatrix m_M_rp = m_R_rp.mult(m_M_init_unbiased);

        //calc heading (rads) from rotated magnetic field
        double h = Math.atan2(-m_M_rp.get(1), m_M_rp.get(0));

        double[][] R_h = {{Math.cos(h), Math.sin(h), 0},
                          {-Math.sin(h), Math.cos(h), 0},
                          {0, 0, 1}};

        //calc complete (initial) rotation matrix by multiplying roll/pitch matrix with heading matrix
        SimpleMatrix m_R_h = new SimpleMatrix(R_h);
        SimpleMatrix m_R = m_R_rp.mult(m_R_h);

        return ExtraFunctions.denseMatrixToArray(m_R.getMatrix());

    }

    private static double[] removeBias(float[] M_init, float[] M_bias) {
        //ignoring the last 3 values of M_init, which are the android-calculated iron biases
        double[] M_biasRemoved = new double[3];
        for (int i = 0; i < 3; i++)
            M_biasRemoved[i] = M_init[i] - M_bias[i];
        return M_biasRemoved;
    }

}
