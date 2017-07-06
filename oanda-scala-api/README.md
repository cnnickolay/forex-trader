# OANDA Scala API
Scala API implementation for OANDA v20 RESTful API

This API can be used for trading automation aka trading with botto.

## Api functions implemented
So far only few APIs was implemented necessary for trading automation.

### AccountsApi
* accounts - Get a list of all Accounts authorized for the provided token.
* accountDetails - Get the full details for a single Account that a client has access to. Full pending Order, open Trade and open Position representations are provided.
* accountSummary - Get a summary for a single Account that a client has access to.
* accountInstruments - Get the list of tradeable instruments for the given Account. The list of tradeable instruments is dependent on the regulatory division that the Account is located in, thus should be the same for all Accounts owned by a single user.
### Instrument
* candles - Fetch candlestick data for an instrument.
### Order
* createOrder - Create an Order for an Account
* orders - Get a list of Orders for an Account
* candelOrder - Cancel a pending Order in an Account
### Trade
* trade - Get a list of Trades for an Account
### Transaction
* transactions - Get a list of Transactions pages that satisfy a time-based Transaction query.
* transactionsIdRange - Get a range of Transactions for an Account based on the Transaction IDs.
* transactionsStream - Get a stream of Transactions for an Account starting from when the request is made.
### Positions
Retrieving current and past positions
* positions - List all Positions for an Account. The Positions returned are for every instrument that has had a position during the lifetime of an the Account.
* openPositions - List all open Positions for an Account. An open Position is a Position in an Account that currently has a Trade opened for it.
* closePosition - Closeout the open Position for a specific instrument in an Account.
### Pricing
Getting historical and realtime pricing data. Streaming is supported
* pricing - Get pricing information for a specified list of Instruments within an Account.
* pricingStream - Get a stream of Account Prices starting from when the request is made. This pricing stream does not include every single price created for the Account, but instead will provide at most 4 prices per second (every 250 milliseconds) for each instrument being requested. If more than one price is created for an instrument during the 250 millisecond window, only the price in effect at the end of the window is sent. This means that during periods of rapid price movement, subscribers to this stream will not be sent every price. Pricing windows for different connections to the price stream are not all aligned in the same way (i.e. they are not all aligned to the top of the second). This means that during periods of rapid price movement, different subscribers may observe different prices depending on their alignment. Note: This endpoint is served by the streaming URLs.

## How to use
1. In src/main/resources copy api-default.properties to api.properties in the same directory.
2. In the properties file copied add your OANDA API token.
3. Use api.Api object to access different APIs.

## Java compatibility
Not implemented yet
