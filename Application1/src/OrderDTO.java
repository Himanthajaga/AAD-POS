import java.util.List;

public class OrderDTO {
    private String orderId;
    private String orderDate;
    private String customerId;
    private String orderAmount;
    private List<ItemDTO> items;

    public OrderDTO() {
    }

    public OrderDTO(String orderId, String orderDate, String customerId, String orderAmount, List<ItemDTO> items) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.customerId = customerId;
        this.orderAmount = orderAmount;
        this.items = items;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getOrderAmount() {
        return orderAmount;
    }

    public List<ItemDTO> getItems() {
        return items;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public void setOrderAmount(String orderAmount) {
        this.orderAmount = orderAmount;
    }

    public void setItems(List<ItemDTO> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "OrderDTO{" +
                "orderId='" + orderId + '\'' +
                ", orderDate='" + orderDate + '\'' +
                ", customerId='" + customerId + '\'' +
                ", orderAmount='" + orderAmount + '\'' +
                ", items=" + items +
                '}';
    }
}
