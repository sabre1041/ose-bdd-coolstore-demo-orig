package com.redhat.coolstore.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateful;
import javax.inject.Inject;

import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.internal.command.CommandFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.RuleServicesClient;

import com.redhat.coolstore.PromoEvent;
import com.redhat.coolstore.Promotion;
import com.redhat.coolstore.ShoppingCart;
import com.redhat.coolstore.ShoppingCartItem;

@Stateful
public class ShoppingCartServiceImplBRMS implements ShoppingCartService, Serializable {

	private static final long serialVersionUID = 6821952169434330759L;

	
	@Inject
	private PromoService promoService;

	public ShoppingCartServiceImplBRMS() {

	}

	@Override
	public void priceShoppingCart(ShoppingCart sc) {

		if (sc != null) {

			ShoppingCart factShoppingCart = new ShoppingCart();

			factShoppingCart.setCartItemPromoSavings(0d);
			factShoppingCart.setCartItemTotal(0d);
			factShoppingCart.setCartTotal(0d);
			factShoppingCart.setShippingPromoSavings(0d);
			factShoppingCart.setShippingTotal(0d);

			// HelloRulesClient.java
			KieServicesConfiguration config = KieServicesFactory.newRestConfiguration(
					"http://testserver2-test.rhel-cdk.10.1.2.2.xip.io/kie-server/services/rest/server", "justin",
					"abcd1234!");
			config.setMarshallingFormat(MarshallingFormat.JSON);
			RuleServicesClient client = KieServicesFactory.newKieServicesClient(config)
					.getServicesClient(RuleServicesClient.class);
			List<Command> commands = new ArrayList<Command>();

			// if at least one shopping cart item exist
			if (sc.getShoppingCartItemList().size() > 0) {

				for (Promotion promo : promoService.getPromotions()) {

					PromoEvent pv = new PromoEvent(promo.getItemId(), promo.getPercentOff());

					commands.add(CommandFactory.newInsert(pv));

				}

				commands.add(CommandFactory.newInsert(factShoppingCart, "shoppingCart"));

				for (ShoppingCartItem sci : sc.getShoppingCartItemList()) {

					com.redhat.coolstore.ShoppingCartItem factShoppingCartItem = new com.redhat.coolstore.ShoppingCartItem();
					factShoppingCartItem.setItemId(sci.getProduct().getItemId());
					factShoppingCartItem.setName(sci.getProduct().getName());
					factShoppingCartItem.setPrice(sci.getProduct().getPrice());
					factShoppingCartItem.setQuantity(sci.getQuantity());
					factShoppingCartItem.setShoppingCart(factShoppingCart);
					factShoppingCartItem.setPromoSavings(0d);

					commands.add(CommandFactory.newInsert(factShoppingCartItem));

				}

				commands.add(CommandFactory.newStartProcess("com.redhat.coolstore.PriceProcess"));
				commands.add(CommandFactory.newFireAllRules());
				BatchExecutionCommand myCommands = CommandFactory.newBatchExecution(commands,
						"defaultStatelessKieSession");
				ServiceResponse<String> response = client.executeCommands("default", myCommands);
				System.out.print(response.getResult());
				System.out.print(response.getMsg());


			}

			sc.setCartItemTotal(factShoppingCart.getCartItemTotal());
			sc.setCartItemPromoSavings(factShoppingCart.getCartItemPromoSavings());
			sc.setShippingTotal(factShoppingCart.getShippingTotal());
			sc.setShippingPromoSavings(factShoppingCart.getShippingPromoSavings());
			sc.setCartTotal(factShoppingCart.getCartTotal());

		}

	}

}
