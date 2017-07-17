# Forex Trader
OANDA powered forex trading bot will make you rich! :-D

### What this bot does?
The bot reads trading data from oanda, calculates different indicators and applies trading strategies. 

#### Goals
The idea is to perform big amount of small trades, each bringing few cents of profit.... and get rich!

#### Indicators
When I'm writing this readme there are only few indicators are implemented, those are:
* SMA - simple moving average
* EMA - exponential moving average
* MACD - Moving Average Convergence Divergence
* RSI - relative strength index
* CMO - chande momentum oscillator
* Stochastic oscillator

Existing java libraries for calculating those indicators are extremely ugly, so I had no choice but to reinvent the wheel here.
#### Trading strategies
So far there is nothing implemented, I only started playing around with data. I'm trying MACD on 5-minute candles and having some decent results.

Later I might try some Machine Learning techniques with Apache Spark. 

### Contact
If you want to contribute with good ideas, email to me and let's discuss getting rich strategies.
