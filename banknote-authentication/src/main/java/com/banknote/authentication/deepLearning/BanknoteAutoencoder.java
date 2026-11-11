package com.banknote.authentication.deepLearning;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ActivationLayer;
import org.deeplearning4j.nn.conf.layers.BatchNormalization;
import org.deeplearning4j.nn.conf.layers.CnnLossLayer;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.conf.layers.Upsampling2D;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * DeepLearning4J CNN Convolutional Autoencoder Architecture.
 * Designed for One-Class Banknote Anomaly Detection on 224x224x3 images.
 * Learns to compress and reconstruct genuine banknote security patterns.
 */
@Component
public class BanknoteAutoencoder {

    private static final Logger logger = LoggerFactory.getLogger(BanknoteAutoencoder.class);

    public static final int INPUT_HEIGHT = 224;
    public static final int INPUT_WIDTH = 224;
    public static final int INPUT_CHANNELS = 3;

    /**
     * Build and initialize a MultiLayerNetwork CNN Autoencoder.
     *
     * @param learningRate Adam optimizer learning rate (e.g. 0.001)
     * @param seed random seed for reproducibility
     * @return initialized MultiLayerNetwork ready for training or loading
     */
    public MultiLayerNetwork buildNetwork(double learningRate, long seed) {
        logger.info("Constructing DeepLearning4J CNN Autoencoder architecture (Input: {}x{}x{})...",
                INPUT_WIDTH, INPUT_HEIGHT, INPUT_CHANNELS);

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .updater(new Adam(learningRate))
                .weightInit(WeightInit.XAVIER)
                .list()
                // ==================== ENCODER ====================
                // Layer 0: Conv2D 3 -> 32 filters (224x224)
                .layer(0, new ConvolutionLayer.Builder(3, 3)
                        .nIn(INPUT_CHANNELS)
                        .nOut(32)
                        .stride(1, 1)
                        .padding(1, 1)
                        .activation(Activation.IDENTITY)
                        .build())
                // Layer 1: Batch Normalization
                .layer(1, new BatchNormalization.Builder().build())
                // Layer 2: Activation ReLU
                .layer(2, new ActivationLayer.Builder()
                        .activation(Activation.RELU).build())
                // Layer 3: Max Pooling 2x2 -> (112x112x32)
                .layer(3, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(2, 2)
                        .build())

                // Layer 4: Conv2D 32 -> 64 filters (112x112)
                .layer(4, new ConvolutionLayer.Builder(3, 3)
                        .nOut(64)
                        .stride(1, 1)
                        .padding(1, 1)
                        .activation(Activation.IDENTITY)
                        .build())
                // Layer 5: Batch Normalization
                .layer(5, new BatchNormalization.Builder().build())
                // Layer 6: Activation ReLU
                .layer(6, new ActivationLayer.Builder()
                        .activation(Activation.RELU).build())
                // Layer 7: Max Pooling 2x2 -> (56x56x64)
                .layer(7, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(2, 2)
                        .build())

                // Layer 8: Conv2D 64 -> 128 filters (56x56)
                .layer(8, new ConvolutionLayer.Builder(3, 3)
                        .nOut(128)
                        .stride(1, 1)
                        .padding(1, 1)
                        .activation(Activation.IDENTITY)
                        .build())
                // Layer 9: Batch Normalization
                .layer(9, new BatchNormalization.Builder().build())
                // Layer 10: Activation ReLU
                .layer(10, new ActivationLayer.Builder()
                        .activation(Activation.RELU).build())
                // Layer 11: Max Pooling 2x2 -> (28x28x128) - Latent Bottleneck
                .layer(11, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2)
                        .stride(2, 2)
                        .build())

                // ==================== DECODER ====================
                // Layer 12: UpSampling 2x2 -> (56x56x128)
                .layer(12, new Upsampling2D.Builder(2).build())
                // Layer 13: Conv2D 128 -> 64 filters (56x56)
                .layer(13, new ConvolutionLayer.Builder(3, 3)
                        .nOut(64)
                        .stride(1, 1)
                        .padding(1, 1)
                        .activation(Activation.IDENTITY)
                        .build())
                // Layer 14: Batch Normalization
                .layer(14, new BatchNormalization.Builder().build())
                // Layer 15: Activation ReLU
                .layer(15, new ActivationLayer.Builder()
                        .activation(Activation.RELU).build())

                // Layer 16: UpSampling 2x2 -> (112x112x64)
                .layer(16, new Upsampling2D.Builder(2).build())
                // Layer 17: Conv2D 64 -> 32 filters (112x112)
                .layer(17, new ConvolutionLayer.Builder(3, 3)
                        .nOut(32)
                        .stride(1, 1)
                        .padding(1, 1)
                        .activation(Activation.IDENTITY)
                        .build())
                // Layer 18: Batch Normalization
                .layer(18, new BatchNormalization.Builder().build())
                // Layer 19: Activation ReLU
                .layer(19, new ActivationLayer.Builder()
                        .activation(Activation.RELU).build())

                // Layer 20: UpSampling 2x2 -> (224x224x32)
                .layer(20, new Upsampling2D.Builder(2).build())
                // Layer 21: Final Conv2D -> 3 filters (224x224x3) with Sigmoid activation
                .layer(21, new ConvolutionLayer.Builder(3, 3)
                        .nOut(INPUT_CHANNELS)
                        .stride(1, 1)
                        .padding(1, 1)
                        .activation(Activation.SIGMOID)
                        .build())
                // Layer 22: CnnLossLayer with Mean Squared Error (MSE)
                .layer(22, new CnnLossLayer.Builder(LossFunctions.LossFunction.MSE).build())
                .setInputType(InputType.convolutional(INPUT_HEIGHT, INPUT_WIDTH, INPUT_CHANNELS))
                .build();

        MultiLayerNetwork network = new MultiLayerNetwork(conf);
        network.init();

        logger.info("CNN Autoencoder initialized successfully. Parameter count: {}", network.numParams());
        return network;
    }
}

