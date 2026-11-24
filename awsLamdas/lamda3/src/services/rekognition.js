const { RekognitionClient, CompareFacesCommand } = require('@aws-sdk/client-rekognition');

const rekognitionClient = new RekognitionClient({ region: 'us-east-1' });

// Esta función acepta un string en Base64 (que es lo que llega por JSON)
// y lo convierte a Buffer para el SDK.
const compareFaces = async (sourceImageBase64, targetImageBase64) => {
    // 1. Limpiamos el prefijo "data:image/jpeg;base64," si existe
    const cleanSource = sourceImageBase64.replace(/^data:image\/\w+;base64,/, "");
    const cleanTarget = targetImageBase64.replace(/^data:image\/\w+;base64,/, "");

    // 2. La magia: convertimos el string Base64 a un Buffer de Node.js (bytes puros)
    const sourceBuffer = Buffer.from(cleanSource, 'base64');
    const targetBuffer = Buffer.from(cleanTarget, 'base64');

    const command = new CompareFacesCommand({
        SourceImage: { Bytes: sourceBuffer }, // Pasamos el Buffer
        TargetImage: { Bytes: targetBuffer }, // Pasamos el Buffer
        SimilarityThreshold: 80
    });

    const response = await rekognitionClient.send(command);

    const isMatch = response.FaceMatches && response.FaceMatches.length > 0;
    const similarity = isMatch ? response.FaceMatches[0].Similarity : 0;

    return { isMatch, similarity };
};

module.exports = { compareFaces };