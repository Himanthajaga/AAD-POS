function loadCustomerCount() {
    const customerCount = customerDatabase.length.toString().padStart(2, '0');
    $('#customerCount').text(customerCount);
}

function loadItemCount() {
    const itemCount = itemDatabase.length.toString().padStart(2, '0');
    $('#itemCount').text(itemCount);
}

function loadOrderCount() {
    $.ajax({
        url: "http://localhost:8080/Application2_Web_exploded/orders",
        success: function (response) {
            const orderCount = response.length.toString().padStart(2, '0');
            $('#orderCount').text(orderCount);
            orderDatabase = response; // Update the orderDatabase array
        },
        error: function (error) {
            console.log(error);
        }
    });
}
