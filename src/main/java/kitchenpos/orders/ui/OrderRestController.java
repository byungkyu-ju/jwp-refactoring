package kitchenpos.orders.ui;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import kitchenpos.orders.application.OrderService;
import kitchenpos.orders.domain.Orders;

@RestController
public class OrderRestController {
	private final OrderService orderService;

	public OrderRestController(final OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping("/api/orders")
	public ResponseEntity<Orders> create(@RequestBody final Orders order) {
		final Orders created = orderService.create(order);
		final URI uri = URI.create("/api/orders/" + created.getId());
		return ResponseEntity.created(uri)
			.body(created)
			;
	}

	@GetMapping("/api/orders")
	public ResponseEntity<List<Orders>> list() {
		return ResponseEntity.ok()
			.body(orderService.list())
			;
	}

	@PutMapping("/api/orders/{orderId}/order-status")
	public ResponseEntity<Orders> changeOrderStatus(
		@PathVariable final Long orderId,
		@RequestBody final Orders order
	) {
		return ResponseEntity.ok(orderService.changeOrderStatus(orderId, order));
	}
}
