import { NativeTools } from 'capacitor-native-tools';

window.testEcho = () => {
    const inputValue = document.getElementById("echoInput").value;
    NativeTools.echo({ value: inputValue })
}
