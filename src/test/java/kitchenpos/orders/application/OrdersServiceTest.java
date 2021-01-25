package kitchenpos.orders.application;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kitchenpos.menu.repository.MenuRepository;
import kitchenpos.menu.repository.MenuGroupRepository;
import kitchenpos.menu.repository.MenuProductRepository;
import kitchenpos.orders.repository.OrderRepository;
import kitchenpos.orders.repository.OrderTableRepository;
import kitchenpos.product.repository.ProductRepository;
import kitchenpos.table.repository.TableGroupDao;
import kitchenpos.menu.domain.Menu;
import kitchenpos.menu.domain.MenuGroup;
import kitchenpos.menu.domain.MenuProduct;
import kitchenpos.orders.domain.OrderStatus;
import kitchenpos.orders.domain.Orders;
import kitchenpos.orders.dto.OrderLineItemRequest;
import kitchenpos.orders.dto.OrderRequest;
import kitchenpos.orders.dto.OrderResponse;
import kitchenpos.product.domain.Product;
import kitchenpos.table.domain.OrderTable;
import kitchenpos.table.domain.TableGroup;
import kitchenpos.utils.IntegrationTest;

/**
 * @author : byungkyu
 * @date : 2021/01/20
 * @description :
 **/
@DisplayName("주문")
class OrdersServiceTest extends IntegrationTest {
	@Autowired
	private OrderService orderService;
	@Autowired
	private MenuRepository menuRepository;
	@Autowired
	private MenuGroupRepository menuGroupRepository;
	@Autowired
	private TableGroupDao tableGroupDao;
	@Autowired
	private OrderTableRepository orderTableRepository;
	@Autowired
	private MenuProductRepository menuProductRepository;
	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private OrderRepository orderRepository;

	@DisplayName("주문을 등록할 수 있다.")
	@Test
	void create(){
		// given
		TableGroup savedTableGroup = tableGroupDao.save(new TableGroup(LocalDateTime.now()));
		OrderTable savedOrderTable = orderTableRepository.save(new OrderTable(savedTableGroup, 3, false));

		MenuGroup savedMenuGroup = menuGroupRepository.save(new MenuGroup("사이드메뉴"));
		Product savedProduct = productRepository.save(new Product("샐러드", BigDecimal.valueOf(9000)));
		Menu savedMenu = menuRepository.save(new Menu("닭가슴살샐러드", BigDecimal.valueOf(9000), savedMenuGroup.getId()));
		MenuProduct savedMenuProduct = menuProductRepository.save(new MenuProduct(savedMenu, savedProduct, 1));
		savedMenu.addMenuProduct(savedMenuProduct);

		OrderLineItemRequest orderLineItemRequest = new OrderLineItemRequest(savedMenu.getId(), 1L);
		OrderRequest orderRequest = new OrderRequest(savedOrderTable.getId(), Arrays.asList(orderLineItemRequest));

		// when
		OrderResponse finalSavedOrder = orderService.create(orderRequest);

		// then
		assertThat(finalSavedOrder.getId()).isNotNull();

	}


	@DisplayName("주문항목이 존재하지 않으면 등록할 수 없다.")
	@Test
	void whenOrderLineItemsIsEmpty(){
		// given
		TableGroup savedTableGroup = tableGroupDao.save(new TableGroup(LocalDateTime.now()));
		OrderTable savedOrderTable = orderTableRepository.save(new OrderTable(savedTableGroup, 3, false));


		MenuGroup savedMenuGroup = menuGroupRepository.save(new MenuGroup("사이드메뉴"));
		Product savedProduct = productRepository.save(new Product("샐러드", BigDecimal.valueOf(9000)));
		Menu savedMenu = menuRepository.save(new Menu("닭가슴살샐러드", BigDecimal.valueOf(9000), savedMenuGroup.getId()));
		MenuProduct savedMenuProduct = menuProductRepository.save(new MenuProduct(savedMenu, savedProduct, 1));
		savedMenu.addMenuProduct(savedMenuProduct);

		OrderRequest orderRequest = new OrderRequest(savedOrderTable.getId(), Collections.EMPTY_LIST);

		// when - then
		assertThatThrownBy(() -> {
			orderService.create(orderRequest);
		}).isInstanceOf(IllegalArgumentException.class);

	}

	@DisplayName("주문항목과 메뉴의 항목들이 일치해야 주문할 수 있다.")
	@Test
	void orderLineItemsSizeMustExistInMenu(){
		// given

		TableGroup savedTableGroup = tableGroupDao.save(new TableGroup(LocalDateTime.now()));
		OrderTable savedOrderTable = orderTableRepository.save(new OrderTable(savedTableGroup, 3, false));

		MenuGroup savedMenuGroup = menuGroupRepository.save(new MenuGroup("사이드메뉴"));
		Product savedProduct = productRepository.save(new Product("샐러드", BigDecimal.valueOf(9000)));
		Menu savedMenu = menuRepository.save(new Menu("닭가슴살샐러드", BigDecimal.valueOf(9000), savedMenuGroup.getId()));
		MenuProduct savedMenuProduct = menuProductRepository.save(new MenuProduct(savedMenu, savedProduct, 1));
		savedMenu.addMenuProduct(savedMenuProduct);

		Long invalidMenuId = 22222L;

		OrderLineItemRequest orderLineItemRequest = new OrderLineItemRequest(savedMenu.getId(), 1L);
		OrderLineItemRequest orderLineItemRequest2 = new OrderLineItemRequest(invalidMenuId, 2L);
		OrderRequest orderRequest = new OrderRequest(savedOrderTable.getId(), Arrays.asList(orderLineItemRequest,orderLineItemRequest2));

		// when - then
		assertThatThrownBy(() -> {
			orderService.create(orderRequest);
		}).isInstanceOf(IllegalArgumentException.class);
	}


	@DisplayName("빈 테이블은 주문을 할 수 없다.")
	@Test
	void empTyOrderTableCannotOrder(){
		// given
		TableGroup savedTableGroup = tableGroupDao.save(new TableGroup(LocalDateTime.now()));
		OrderTable savedOrderTable = orderTableRepository.save(new OrderTable(savedTableGroup, 3, true));

		MenuGroup savedMenuGroup = menuGroupRepository.save(new MenuGroup("사이드메뉴"));
		Product savedProduct = productRepository.save(new Product("샐러드", BigDecimal.valueOf(9000)));
		Menu savedMenu = menuRepository.save(new Menu("닭가슴살샐러드", BigDecimal.valueOf(9000), savedMenuGroup.getId()));
		MenuProduct savedMenuProduct = menuProductRepository.save(new MenuProduct(savedMenu, savedProduct, 1));
		savedMenu.addMenuProduct(savedMenuProduct);

		OrderLineItemRequest orderLineItemRequest = new OrderLineItemRequest(savedMenu.getId(), 1L);
		OrderRequest orderRequest = new OrderRequest(savedOrderTable.getId(), Arrays.asList(orderLineItemRequest));

		// when - then
		assertThatThrownBy(() -> {
			orderService.create(orderRequest);
		}).isInstanceOf(IllegalArgumentException.class);
	}

	@DisplayName("주문의 목록을 조회할 수 있다.")
	@Test
	void list(){
		// when
		List<OrderResponse> orders = orderService.list();

		List<Orders> actualOrders = orderRepository.findAll();

		List<Long> actualOrderIds = actualOrders.stream()
			.map(Orders::getId)
			.collect(Collectors.toList());

		// then
		assertThat(orders).isNotEmpty();

		assertThat(actualOrderIds).containsAll(actualOrderIds);
	}



	@DisplayName("주문의 상태를 변경할 수 있다.")
	@Test
	void changeOrderStatus() {
		TableGroup savedTableGroup = tableGroupDao.save(new TableGroup(LocalDateTime.now()));
		OrderTable savedOrderTable = orderTableRepository.save(new OrderTable(savedTableGroup, 3, false));


		MenuGroup savedMenuGroup = menuGroupRepository.save(new MenuGroup("사이드메뉴"));
		Product savedProduct = productRepository.save(new Product("샐러드", BigDecimal.valueOf(9000)));
		Menu savedMenu = menuRepository.save(new Menu("닭가슴살샐러드", BigDecimal.valueOf(9000), savedMenuGroup.getId()));
		MenuProduct savedMenuProduct = menuProductRepository.save(new MenuProduct(savedMenu, savedProduct, 1));
		savedMenu.addMenuProduct(savedMenuProduct);

		OrderLineItemRequest orderLineItemRequest = new OrderLineItemRequest(savedMenu.getId(), 1L);
		OrderRequest orderRequest = new OrderRequest(savedOrderTable.getId(), Arrays.asList(orderLineItemRequest));

		OrderResponse finalSavedOrder = orderService.create(orderRequest);

		OrderRequest changeOrder = new OrderRequest(OrderStatus.COMPLETION.name());
		// when
		OrderResponse changedOrder = orderService.changeOrderStatus(finalSavedOrder.getId(), changeOrder);

		// then
		assertThat(changedOrder.getOrderStatus()).isEqualTo(OrderStatus.COMPLETION.name());
	}


	@DisplayName("완료된 주문은 상태를 변경할 수 없다.")
	@Test
	void completedOrderCannotChange(){
		// given
		TableGroup savedTableGroup = tableGroupDao.save(new TableGroup(LocalDateTime.now()));
		OrderTable savedOrderTable = orderTableRepository.save(new OrderTable(savedTableGroup, 3, false));

		MenuGroup savedMenuGroup = menuGroupRepository.save(new MenuGroup("사이드메뉴"));
		Product savedProduct = productRepository.save(new Product("샐러드", BigDecimal.valueOf(9000)));
		Menu savedMenu = menuRepository.save(new Menu("닭가슴살샐러드", BigDecimal.valueOf(9000), savedMenuGroup.getId()));
		MenuProduct savedMenuProduct = menuProductRepository.save(new MenuProduct(savedMenu, savedProduct, 1));
		savedMenu.addMenuProduct(savedMenuProduct);

		OrderLineItemRequest orderLineItemRequest = new OrderLineItemRequest(savedMenu.getId(), 1L);
		OrderRequest orderRequest = new OrderRequest(savedOrderTable.getId(), Arrays.asList(orderLineItemRequest));
		OrderResponse finalSavedOrder = orderService.create(orderRequest);

		OrderRequest changeOrder = new OrderRequest(OrderStatus.COMPLETION.name());
		orderService.changeOrderStatus(finalSavedOrder.getId(), changeOrder);

		// when - then
		assertThatThrownBy(() -> {
			orderService.changeOrderStatus(finalSavedOrder.getId(), changeOrder);
		}).isInstanceOf(IllegalArgumentException.class);
	}


}