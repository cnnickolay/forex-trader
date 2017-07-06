package org.nikosoft.oanda;

import org.nikosoft.oanda.api.Api;
import org.nikosoft.oanda.api.Errors;
import org.nikosoft.oanda.api.def.AccountsApi;

public class Examples {

  public static void main(String[] args) {
    AccountsApi accountsApi = Api.accounts();
//    scalaz.$bslash$div<Errors.Error, AccountsApi.AccountsResponse> accounts = accountsApi.accounts();
    // scalaz does not help Java integration ... :-/
  }

}
