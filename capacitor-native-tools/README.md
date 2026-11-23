# capacitor-native-tools

tool nativa de capacitor

## Install

To use npm

```bash
npm install capacitor-native-tools
````

To use yarn

```bash
yarn add capacitor-native-tools
```

Sync native files

```bash
npx cap sync
```

## API

<docgen-index>

* [`echo(...)`](#echo)
* [`showNativeAlert()`](#shownativealert)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### echo(...)

```typescript
echo(options: { value: string; }) => Promise<{ value: string; }>
```

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>{ value: string; }</code> |

**Returns:** <code>Promise&lt;{ value: string; }&gt;</code>

--------------------


### showNativeAlert()

```typescript
showNativeAlert() => Promise<{ action: string; }>
```

**Returns:** <code>Promise&lt;{ action: string; }&gt;</code>

--------------------

</docgen-api>
