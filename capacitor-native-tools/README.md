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
* [`startFaceLiveness(...)`](#startfaceliveness)

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


### startFaceLiveness(...)

```typescript
startFaceLiveness(options: { sessionId: string; region?: string; }) => Promise<{ status: 'complete' | 'error' | 'cancelled'; result?: string; error?: string; }>
```

| Param         | Type                                                 |
| ------------- | ---------------------------------------------------- |
| **`options`** | <code>{ sessionId: string; region?: string; }</code> |

**Returns:** <code>Promise&lt;{ status: 'error' | 'complete' | 'cancelled'; result?: string; error?: string; }&gt;</code>

--------------------

</docgen-api>
