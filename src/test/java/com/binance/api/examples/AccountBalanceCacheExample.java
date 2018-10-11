package com.binance.api.examples;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.account.AssetBalance;

import com.binance.api.client.domain.account.Trade;
import com.binance.api.client.domain.general.SymbolInfo;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.binance.api.client.domain.event.UserDataUpdateEvent.UserDataUpdateEventType.ACCOUNT_UPDATE;

/**
 * Illustrates how to use the user data event stream to create a local cache for the balance of an account.
 */
public class AccountBalanceCacheExample {

  private final BinanceApiClientFactory clientFactory;

  /**
   * Key is the symbol, and the value is the balance of that symbol on the account.
   */
  private Map<String, AssetBalance> accountBalanceCache;

  /**
   * Listen key used to interact with the user data streaming API.
   */
  private final String listenKey;

  public AccountBalanceCacheExample(String apiKey, String secret) {
    this.clientFactory = BinanceApiClientFactory.newInstance(apiKey, secret);
    this.listenKey = initializeAssetBalanceCacheAndStreamSession();
    startAccountBalanceEventStreaming(listenKey);
  }

  /**
   * Initializes the asset balance cache by using the REST API and starts a new user data streaming session.
   *
   * @return a listenKey that can be used with the user data streaming API.
   */
  private String initializeAssetBalanceCacheAndStreamSession() {
    BinanceApiRestClient client = clientFactory.newRestClient();
    Account account = client.getAccount();

    this.accountBalanceCache = new TreeMap<>();
    for (AssetBalance assetBalance : account.getBalances()) {
      accountBalanceCache.put(assetBalance.getAsset(), assetBalance);
    }

    return client.startUserDataStream();
  }

  /**
   * Begins streaming of agg trades events.
   */
  private void startAccountBalanceEventStreaming(String listenKey) {
    BinanceApiWebSocketClient client = clientFactory.newWebSocketClient();

    client.onUserDataUpdateEvent(listenKey, response -> {
      if (response.getEventType() == ACCOUNT_UPDATE) {
        // Override cached asset balances
        for (AssetBalance assetBalance : response.getAccountUpdateEvent().getBalances()) {
          accountBalanceCache.put(assetBalance.getAsset(), assetBalance);
        }
        System.out.println(accountBalanceCache);
      }
    });
  }

  /**
   * @return an account balance cache, containing the balance for every asset in this account.
   */
  public Map<String, AssetBalance> getAccountBalanceCache() {
    return accountBalanceCache;
  }

  public static void main(String[] args) {
//    new AccountBalanceCacheExample("YOUR_API_KEY", "YOUR_SECRET");

    String apiKeyOne = "Xe4qyuwHMAROHOtRKbATdLVfZpFCB3w6Q717CgM6Ut2HivwkQg5PWWKTHtAPNQPD";
    String secretOne = "NiHwfzQHCzTmjsReEzhSWkqH6iU10H1VDFO4FHxVdSXgYVXQeT7N7ShCKqGVuMyR";
    System.out.println(new String(Base64.getEncoder().encode(apiKeyOne.getBytes())));
    System.out.println(new String(Base64.getEncoder().encode(secretOne.getBytes())));

    String apiKeyTwo = "b4APbRTUEolX2tluV3AVKWER6JSjH527x7pRqIUWfnHIQJ5JoR3RP8kLOhfaUqDM";
    String secretTwo = "ONvXr1rHMcygnELS0z0gEl9HlWBxJIHEbraceU0BRWwShXxWQdxMZlTQ8fJWGpUS";
    System.out.println(new String(Base64.getEncoder().encode(apiKeyTwo.getBytes())));
    System.out.println(new String(Base64.getEncoder().encode(secretTwo.getBytes())));

    BinanceApiRestClient binanceApiRestClient = BinanceApiClientFactory.newInstance(apiKeyOne, secretOne)
        .newRestClient();

    List<SymbolInfo> symbols = binanceApiRestClient.getExchangeInfo().getSymbols();

    List<SymbolInfo> symbolInfos = filterSymbols("ETH", symbols);
    for (SymbolInfo symbolInfo : symbolInfos) {
      List<Trade> myTrades = binanceApiRestClient.getMyTrades(symbolInfo.getSymbol());
      System.out.println(myTrades.size());
    }

    Account account = binanceApiRestClient.getAccount();
    System.out.println(account.getBalances());

    Account account1 = BinanceApiClientFactory.newInstance(apiKeyTwo, secretTwo).newRestClient().getAccount();
    System.out.println(account1.getBalances());
  }

  private static List<SymbolInfo> filterSymbols(String eth, List<SymbolInfo> symbols) {
    List<SymbolInfo> symbolInfos = new ArrayList<>();

    for (SymbolInfo symbol : symbols) {
      if(symbol.getSymbol().toLowerCase().startsWith(eth.toLowerCase())) {
          symbolInfos.add(symbol);
      }
    }
    return symbolInfos;
  }
}
