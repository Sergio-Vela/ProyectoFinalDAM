const { compareFaces } = require('./services/rekognition');
const { buildResponse } = require('./utils/response');

exports.handler = async (event) => {
    console.log('Evento entrante a Lambda 3');

    const httpMethod = event.httpMethod || event.requestContext?.http?.method;

    // Manejo de CORS preflight
    if (httpMethod === 'OPTIONS') {
        return buildResponse(200, { message: 'Preflight check' });
    }

    let body;
    try {
        body = JSON.parse(event.body);
    } catch (error) {
        return buildResponse(400, { message: 'Invalid JSON in request body' });
    }

    const { sourceImageBase64, targetImageBase64 } = body;

    if (!sourceImageBase64 || !targetImageBase64) {
        return buildResponse(400, { message: 'Faltan las imágenes para comparar (source y target)' });
    }

    try {
        const result = await compareFaces(sourceImageBase64, targetImageBase64);
        console.log('Resultado de comparación:', result);
        
        return buildResponse(200, result);
    } catch (error) {
        console.error('Error comparando rostros:', error);
        return buildResponse(500, { message: 'Error interno comparando rostros' });
    }
};