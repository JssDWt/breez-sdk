/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow strict-local
 */

import React, { useState } from "react"
import { SafeAreaView, ScrollView, StatusBar, Text, View } from "react-native"
import { mnemonicToSeed, parseInvoice } from "react-native-breez-sdk"

const TEXT_MNEMONIC =
    "reveal man culture nominee tag abuse keen behave refuse warfare crisp thunder " +
    "valve knock unique try fold energy torch news thought access hawk table"

const App = () => {
    const [seed, setSeed] = useState()
    const [invoice, setInvoice] = useState()

    React.useEffect(() => {
        const asyncFn = async () => {
            console.log(`Mnemonic: ${TEXT_MNEMONIC}`)
            const seedResponse = await mnemonicToSeed(TEXT_MNEMONIC)
            console.log(`seedResponse: ${seedResponse}`)
            setSeed(JSON.stringify(seedResponse))

            const invoiceResponse = await parseInvoice(
                "lnbc1p37jj09pp5rt6san90t25jn67kf8jm2stp4x6rfh7hsuffeqta6zllnmlumccqdqqcqzryxqyz5vqsp" +
                    "5xq7eyzeel4errazca6xh9htmlsjmwzfmvyhxhjftkjstjtyaqlnq9qyyssqt5e0kgw6z0px5phk3zq" +
                    "pxm9es0v0v6f8dsj9z30z4ck8tyk4fq4klt5j7u8r3sg8ncqujqa38z65u820hcf9vxl67zst4s9sky7wyaqq0dlsed"
            )
            console.log(`invoiceResponse: ${invoiceResponse}`)
            setInvoice(JSON.stringify(invoiceResponse))
        }
        asyncFn()
    }, [])

    return (
        <SafeAreaView>
            <StatusBar />
            <ScrollView contentInsetAdjustmentBehavior="automatic">
                <View style={{ backgroundColor: "white" }}>
                    <Text>Seed: {`${seed}`}</Text>
                    <Text>Invoice: {`${invoice}`}</Text>
                </View>
            </ScrollView>
        </SafeAreaView>
    )
}

export default App
