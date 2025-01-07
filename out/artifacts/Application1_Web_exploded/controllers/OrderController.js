let orderDatabase = [];

initializeCurrentDate();
loadOrderDatabase();

// -------------------------------------- Validations & Form Control --------------------------------------

// Event delegation setup
$('#order-detail-tbody').on('click', '.text-danger', function () {
    $(this).closest('tr').remove();  // Only removes the tr containing the clicked button
    initializeTotalAndSubtotal();
});

$('#tab-content-3 input[type="date"], #tab-content-3 input[pattern]').on('input change', realTimeValidate);

$('#txt-order-qty').on('input', validateOrderQuantity);

$('#txt-order-cash').on('input', validateOrderCash);


// -------------------------------------- Other Functions --------------------------------------

function loadOrderDatabase() {
    $.ajax({
        url: "http://localhost:8080/Application1_Web_exploded/orders",
        success: function (response) {
            orderDatabase = response;

            initializeNextOrderId();
            loadOrderCount();
        },
        error: function (error) {
            console.log(error)
        }
    })
}

function initializeNextOrderId() {
    const prevCode = orderDatabase.length > 0 ? orderDatabase[orderDatabase.length - 1].orderId : 'O000';
    const nextCode = generateNextID(prevCode);
    $('#txt-order-id').val(nextCode);
    $('#txt-order-id').removeClass('is-invalid').addClass('is-valid');
}

function initializeCurrentDate() {
    const currentDate = new Date().toISOString().split('T')[0];
    $('#txt-order-date').val(currentDate);
    $('#txt-order-date').removeClass('is-invalid').addClass('is-valid');
}

function initializeOrderComboBoxes() {
    // Clear existing options first (keeping the first empty/default option if exists)
    $('#select-customer-id').find('option:not(:first)').remove();
    $('#select-item-code').find('option:not(:first)').remove();

    customerDatabase.forEach(c => {
        $('#select-customer-id').append(`<option value="${c.id}">${c.id}</option>`);
    });

    itemDatabase.forEach(i => {
        $('#select-item-code').append(`<option value="${i.itemCode}">${i.itemCode}</option>`);
    });
}

// Recalculate total, subtotal, and balance (unchanged)
function initializeTotalAndSubtotal() {
    const rows = $('#order-detail-tbody tr').toArray();
    const total = rows.reduce((acc, row) => {
        const cellValue = $(row).find('td:eq(4)').text().trim();
        const cellAmount = parseFloat(cellValue) || 0;
        return acc + cellAmount;
    }, 0);

    // Display the total
    $('#lbl-total').text("Total: Rs. " + total.toFixed(2) + "/=");

    // Apply discount and calculate subtotal
    let subtotal = total;
    const discountValue = parseFloat($('#txt-order-discount').val()) || 0;
    if (discountValue >= 0 && discountValue <= 100) {
        const discountAmount = (discountValue / 100) * total;
        subtotal = total - discountAmount;
    }

    // Display the subtotal
    $('#lbl-subtotal').text("Sub Total: Rs. " + subtotal.toFixed(2) + "/=");

    // Recalculate balance based on cash entered
    const cashValue = parseFloat($('#txt-order-cash').val()) || 0;
    const balanceField = $('#txt-order-balance');
    if (cashValue >= subtotal) {
        const balance = cashValue - subtotal;
        balanceField.val(balance.toFixed(2)); // Show balance in balance field
    } else {
        balanceField.val(''); // Clear balance if cash is insufficient
    }
}

function getOrderById(id) {
    return orderDatabase.find(o => o.orderId === id);
}

function appendToOrderTable(orderDetail) {
    const item = getItemByCode(orderDetail.itemCode);
    const existingRow = $(`#order-detail-tbody tr td:first-child:contains('${orderDetail.itemCode}')`).closest('tr');

    if (existingRow.length > 0) {
        const currentQty = parseInt(existingRow.find('td:eq(3)').text());
        const newQty = currentQty + parseInt(orderDetail.qty);

        if (newQty > item.qtyOnHand) {
            showToast('error', 'Quantity exceeds available stock !');
        } else {
            // Update existing row
            const newTotal = parseFloat(item.unitPrice * newQty).toFixed(2);

            existingRow.find('td:eq(3)').text(newQty);
            existingRow.find('td:eq(4)').text(newTotal);

            $('#item-select input, #item-select select').removeClass('is-valid').val('');
        }
    } else {
        // Add new row
        $('#order-detail-tbody').append(`
            <tr class="text-center">
                <td>${orderDetail.itemCode}</td>
                <td>${item.description}</td>
                <td>${parseFloat(item.unitPrice).toFixed(2)}</td>
                <td>${orderDetail.qty}</td>
                <td>${parseFloat(item.unitPrice * orderDetail.qty).toFixed(2)}</td>
                <td><button class="btn text-danger"><i class="fa-regular fa-trash-can"></i></button></td>
            </tr>
        `);
        $('#item-select input, #item-select select').removeClass('is-valid').val('');
    }
}

function validateOrderQuantity() {
    const input = $(this);
    const qty = parseInt(input.val());
    const pattern = new RegExp(input.attr('pattern'));
    const itemCode = $('#txt-item-code').val();
    const remainingQty = getRemainingQuantity(itemCode);

    // First check pattern validation
    if (!pattern.test(input.val())) {
        input.removeClass('is-valid').addClass('is-invalid');
        input.next().text('Enter a valid Quantity').show();
        return;
    }

    // Then check quantity validation
    if (qty > 0 && qty <= remainingQty) {
        input.removeClass('is-invalid').addClass('is-valid');
        input.next().hide();
    } else if (qty > remainingQty) {
        input.removeClass('is-valid').addClass('is-invalid');
        input.next().text('Quantity exceeds available stock').show();
    } else {
        input.removeClass('is-valid').addClass('is-invalid');
        input.next().text('Enter a valid Quantity').show();
    }
}

function validateOrderCash() {
    const input = $(this);
    const cash = parseFloat(input.val());
    const pattern = new RegExp(input.attr('pattern'));

    if (!pattern.test(input.val())) {
        input.removeClass('is-valid').addClass('is-invalid');
        input.next().text('Enter a valid Price').show();
        return;
    }

    // Reinitialize totals and balance
    initializeTotalAndSubtotal();
}

function clearForm() {
    // Clear customer fields
    $('#select-customer-id').val('');
    $('#txt-customer-id, #txt-customer-name, #txt-customer-address, #txt-customer-cno').val('').removeClass('is-valid is-invalid');

    // Clear item fields
    $('#select-item-code').val('');
    $('#txt-item-code, #txt-item-name, #txt-item-price, #txt-item-qty, #txt-order-qty').val('').removeClass('is-valid is-invalid');

    // Clear payment fields
    $('#txt-order-cash, #txt-order-discount, #txt-order-balance').val('').removeClass('is-valid is-invalid');

    // Clear table and totals
    $('#order-detail-tbody').empty();
    $('#lbl-total').text('Total: Rs. 0/=');
    $('#lbl-subtotal').text('Sub Total: Rs. 0/=');

    // Reset order ID and date
    initializeNextOrderId();
    initializeCurrentDate();
}

// -------------------------------------- CRUD Operations --------------------------------------

// Place Order
$('#order-purchase-btn').on('click', function () {
    // Validate required fields
    const orderId = $('#txt-order-id').val();
    const date = $('#txt-order-date').val();
    const customerId = $('#txt-customer-id').val();
    const discount = parseFloat($('#txt-order-discount').val()) || 0;
    const cash = parseFloat($('#txt-order-cash').val()) || 0;
    const balance = parseFloat($('#txt-order-balance').val()) || 0;

    // Get items from table
    const items = [];
    $('#order-detail-tbody tr').each(function() {
        items.push({
            itemCode: $(this).find('td:eq(0)').text(),
            qty: parseInt($(this).find('td:eq(3)').text())
        });
    });

    // Validate order
    if (!orderId || !date || !customerId || items.length === 0) {
        showToast('error', 'Please fill all required fields');
        return;
    }

    // Show confirmation modal
    $('#confirm-model').modal('show');
    $('#confirm-model .modal-body').text('Are you sure you want to place this order?');

    // Handle confirmation
    $('#confirm-btn').off('click').on('click', function() {
        const orderData = {
            orderId,
            date,
            customerId,
            discount,
            cash,
            balance,
            items
        };

        $.ajax({
            url: 'http://localhost:8080/Application1_Web_exploded/orders',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(orderData),
            success: function(response) {
                $('#confirm-model').modal('hide');

                if (response.status === 'success') {
                    showToast('success', 'Order placed successfully!');
                    clearForm();
                    loadOrderCount(); // Update order count immediately
                } else {
                    showToast('error', response.message || 'Failed to place order');
                }
            },
            error: function(xhr, status, error) {
                $('#confirm-model').modal('hide');
                showToast('error', 'Failed to place order: ' + error);
            }
        });
    });
});

// Search Order
$('#search-order').on('submit', function (event) {
    event.preventDefault();

    const orderId = $('#txt-search-order').val();
    const order = getOrderById(orderId);

    let isValidated = $('#txt-search-order').hasClass('is-valid');

    if (isValidated) {
        if (order) {
            console.log(order)
            clearForm();

            $('#txt-order-id').val(order.orderId);
            $('#txt-order-date').val(order.date);
            $('#txt-customer-id').val(order.customerId).trigger('input');
            $('#txt-order-discount').val(order.discount);
            $('#txt-order-cash').val(order.cash);
            $('#txt-order-balance').val(order.balance);

            $('#txt-order-id, #txt-order-date, #txt-customer-id, #txt-order-discount, #txt-order-cash, #txt-order-balance').addClass('is-valid').removeClass('is-invalid');

            order.orderDetail.forEach(detail => appendToOrderTable(detail));
            initializeTotalAndSubtotal();
            showToast('success', 'Order search completed successfully !');
        } else {
            showToast('error', 'Order not found !');
        }
    }
});

// Clear Order
$('#clear-order-btn').on('click', function () {
    clearForm();
});

// Select Customer (Load Customer Details)
$('#select-customer-id, #txt-customer-id').on('input change', function () {
    const customerId = $(this).val();
    const customer = getCustomerById(customerId);
    if (customer) {
        $('#select-customer-id, #txt-customer-id').val(customer.id);
        $('#txt-customer-name').val(customer.name);
        $('#txt-customer-address').val(customer.address);
        $('#txt-customer-cno').val(customer.phone);
        $('#txt-customer-id, #txt-customer-name, #txt-customer-address, #txt-customer-cno').removeClass('is-invalid').addClass('is-valid');
    } else {
        $('#select-customer-id, #txt-customer-name, #txt-customer-address, #txt-customer-cno').removeClass('is-valid').val('');
        $('#txt-customer-id').val(customerId);
    }
});

// Select Item (Load Item Details)
$('#select-item-code, #txt-item-code').on('input change', function () {
    const itemCode = $(this).val();
    const item = getItemByCode(itemCode);
    if (item) {
        $('#select-item-code, #txt-item-code').val(item.itemCode);
        $('#txt-item-name').val(item.description);
        $('#txt-item-price').val(parseFloat(item.unitPrice).toFixed(2));

        // Show remaining quantity instead of total quantity
        const remainingQty = getRemainingQuantity(itemCode);
        $('#txt-item-qty').val(remainingQty);

        $('#txt-item-code, #txt-item-name, #txt-item-price, #txt-item-qty').removeClass('is-invalid').addClass('is-valid');
    } else {
        $('#select-item-code, #txt-item-name, #txt-item-price, #txt-item-qty').removeClass('is-valid').val('');
        $('#txt-item-code').val(itemCode);
    }
});

// Add Item
$('#item-select').on('submit', function (event) {
    event.preventDefault();

    let isValidated = $('#item-select input').toArray().every(element => $(element).hasClass('is-valid'));

    if (isValidated) {
        const itemCode = $('#txt-item-code').val();
        const qty = parseInt($('#txt-order-qty').val());

        if (itemDatabase.some(i => i.itemCode === itemCode)) {
            const orderDetail = {itemCode: itemCode, qty: qty};
            appendToOrderTable(orderDetail);
            initializeTotalAndSubtotal();
        } else {
            showToast('error', 'Item not found !');
        }
    }
});

////////////////////////////////////////////////////////////////////////////////
// Add Item to Cart
$('#add-item-btn').on('click', function () {
    const itemCode = $('#txt-item-code').val().trim();
    const orderQty = parseInt($('#txt-order-qty').val());

    // Validate inputs
    if (!itemCode || isNaN(orderQty) || orderQty <= 0) {
        showToast('error', 'Please enter a valid item code and quantity.');
        return;
    }

    // Fetch item details
    const item = getItemByCode(itemCode);
    if (!item) {
        showToast('error', 'Item not found!');
        return;
    }

    // Check stock availability
    if (orderQty > item.qtyOnHand) {
        showToast('error', 'Quantity exceeds available stock!');
        return;
    }

    // Create orderDetail object
    const orderDetail = {
        itemCode: itemCode,
        qty: orderQty,
    };

    // Add or update the table
    appendToOrderTable(orderDetail);

    // Update totals and subtotals
    initializeTotalAndSubtotal();

    // Clear inputs after adding
    $('#txt-item-code, #txt-order-qty').val('');
    $('#txt-item-name, #txt-item-price, #txt-item-qty').val('');
    $('#select-item-code').val('');
});

// Listen for input changes on the discount field
$('#txt-order-discount').on('input', function () {
    const discountValue = parseFloat($(this).val()) || 0; // Get the discount value

    // Ensure the discount is valid (between 0 and 100)
    if (discountValue < 0 || discountValue > 100) {
        // Optionally show a message if the discount is invalid
        $(this).addClass('is-invalid').removeClass('is-valid');
        $(this).next().text('Discount must be between 0 and 100').show();
    } else {
        $(this).removeClass('is-invalid').addClass('is-valid');
        $(this).next().hide();
    }

    // Recalculate the total, subtotal, and balance after the discount change
    initializeTotalAndSubtotal();
});

$('#txt-order-cash, #txt-order-discount').on('input', function() {
    const cash = parseFloat($('#txt-order-cash').val()) || 0;
    const discount = parseFloat($('#txt-order-discount').val()) || 0;
    const total = parseFloat($('#lbl-total').text().replace('Total: Rs. ', '').replace('/=', '')) || 0;
    const finalAmount = total * (1 - discount/100);
    const balance = cash - finalAmount;

    $('#txt-order-balance').val(balance.toFixed(2));
});


////////////////////////
// Function to calculate remaining quantity for an item
function getRemainingQuantity(itemCode) {
    const item = getItemByCode(itemCode);
    if (!item) return 0;

    // Get quantity in cart
    const cartRow = $(`#order-detail-tbody tr td:first-child:contains('${itemCode}')`).closest('tr');
    const cartQty = cartRow.length ? parseInt(cartRow.find('td:eq(3)').text()) : 0;

    // Return remaining quantity
    return item.qtyOnHand - cartQty;
}


