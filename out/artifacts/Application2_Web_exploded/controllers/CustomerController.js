let customerDatabase = [];

loadCustomerDatabase();

// -------------------------------------- Validations & Form Control --------------------------------------

// Validate with regex pattern for Customer ID and Contact Number in real-time
$('#save-customer input, #save-customer textarea').on('input change', realTimeValidate);
$('#update-customer input, #update-customer textarea').on('input change', realTimeValidate);

// Validate with regex pattern for Search Customer in real-time
$('#txt-search-valuec').on('input change', function () {
    const value = $(this).val();
    const option = $('#search-customer-by').val();

    // Define regex patterns for each option
    const patterns = {
        'ID': /^C[0-9]{3,}$/,
        'Name': /^([A-Z]\.[A-Z]\. )?[A-Z][a-z]*( [A-Z][a-z]*)*$/,
        'Contact': /^[0-9]{10}$/
    };

    if (patterns[option]) {
        realTimeValidateInput(value, patterns[option], this);
    }
});

$('#search-customer-by').on('change', function () {
    const value = $('#txt-search-valuec').val();
    const option = $(this).val();

    const patterns = {
        'ID': /^C[0-9]{3,}$/,
        'Name': /^([A-Z]\.[A-Z]\. )?[A-Z][a-z]*( [A-Z][a-z]*)*$/,
        'Contact': /^[0-9]{10}$/
    };

    if (patterns[option]) {
        realTimeValidateInput(value, patterns[option], '#txt-search-valuec');
    }
});

// Reset form validation when Close button is clicked
$('#close-savec-btn, #close-savec-icon').on('click', function () { 
    resetForm('#save-customer', '#save-customer input, #save-customer textarea');
    initializeNextCustomerId();
});
$('#close-updatec-btn, #close-updatec-icon').on('click', function () { 
    resetForm('#update-customer', '#update-customer input, #update-customer textarea');
});

// -------------------------------------- Other Functions --------------------------------------

function loadCustomerDatabase() {
    $.ajax({
        url: "http://localhost:8080/Application2_Web_exploded/customer",
        success: function (response) {
            customerDatabase = response;

            // Initialize Customer ID in New Customer on page load
            initializeNextCustomerId();
            initializeOrderComboBoxes();
            // Load all customers to the table on page load
            loadAllCustomers();
            loadCustomerCount();
        },
        error: function (error) {
            console.log(error)
        }
    })
}

function initializeNextCustomerId() {
    // Initialize Customer ID in New Customer
    const prevId = customerDatabase.length > 0 ? customerDatabase[customerDatabase.length - 1].id : 'C000';
    const nextId = generateNextID(prevId);
    $('#txt-save-cid').val(nextId);
    $('#txt-save-cid').removeClass('is-invalid').addClass('is-valid');
}

function getCustomerByName(name) {
    return customerDatabase.filter(c => c.name.toLowerCase() === name.toLowerCase());
}

function getCustomerByContactNo(contactNo) {
    return customerDatabase.filter(c => c.phone === contactNo);
}

// Function to append customer to table
function appendToCustomerTable(customer) {
    $('#customer-tbody').append(`
        <tr>
            <td>${customer.id}</td>
            <td>${customer.name}</td>
            <td>${customer.address}</td>
            <td>${customer.phone}</td>
        </tr>
    `);
}

// Function to get customer based on option
function getCustomerByOption(option, value) {
    console.log("Option:", option, "Value:", value);  // Debug log

    if (option === 'ID') return getCustomerById(value);
    if (option === 'Name') return getCustomerByName(value);
    if (option === 'Contact') return getCustomerByContactNo(value);

    return null;
}

// Function to load all customers to the table
function loadAllCustomers() {
    $('#customer-tbody').empty();
    for (const customer of customerDatabase) {
        appendToCustomerTable(customer);
    }
}

// Sort Customer Database by ID
function sortCustomerDatabaseById() {
    customerDatabase.sort(function(a, b) {
        const idA = parseInt(a.id.replace('C', ''), 10);  // Remove 'C' and parse as integer
        const idB = parseInt(b.id.replace('C', ''), 10);  // Remove 'C' and parse as integer

        return idA - idB;  // Sort in ascending order
    });
}

// -------------------------------------- CRUD Operations --------------------------------------

// Save Customer
$('#save-customer').on('submit', function (event) {
    event.preventDefault();

    let isValidated = $('#save-customer input, #save-customer textarea').toArray().every(element => $(element).hasClass('is-valid'));

    if (isValidated) {
        const id = $('#txt-save-cid').val();
        const name = $('#txt-save-cname').val();
        const address = $('#txt-save-caddress').val();
        const contactNo = $('#txt-save-cno').val();

        if (!customerDatabase.some(c => c.id === id)) {
            $.ajax({
                url: "http://localhost:8080/Application2_Web_exploded/customer",
                method: "POST",
                data: {
                    id: id,
                    name: name,
                    phone: contactNo,
                    address: address
                },

                success: function (response) {
                    showToast('success', 'Customer saved successfully !');
                    resetForm('#save-customer', '#save-customer input, #save-customer textarea');
                    loadCustomerDatabase();
                    sortCustomerDatabaseById();
                    loadCustomerCount();
                    initializeOrderComboBoxes();
                },
                error: function (error) {
                    console.log(error)
                }
            });
        } else {
            showToast('error', 'Customer ID already exists !');
        }
    }
});

// Update Customer (Load Customer Details)
$('#txt-update-cid').on('input', function (event) {
    if (customerDatabase.some(c => c.id === $(this).val())) {
        const customer = getCustomerById($(this).val());
        $('#txt-update-cname').val(customer.name);
        $('#txt-update-caddress').val(customer.address);
        $('#txt-update-cno').val(customer.phone);
        $('#update-customer input, #update-customer textarea').addClass('is-valid').removeClass('is-invalid');
    } else {
        $('#txt-update-cname').val('');
        $('#txt-update-caddress').val('');
        $('#txt-update-cno').val('');
        $('#update-customer input, #update-customer textarea').removeClass('is-valid');
    }
});

// Update Customer
$('#update-customer').on('submit', function (event) {
    event.preventDefault();

    let isValidated = $('#update-customer input, #update-customer textarea').toArray().every(element => $(element).hasClass('is-valid'));

    if (isValidated) {
        const id = $('#txt-update-cid').val();
        const name = $('#txt-update-cname').val();
        const address = $('#txt-update-caddress').val();
        const contactNo = $('#txt-update-cno').val();

        if (customerDatabase.some(c => c.id === id)) {
            $.ajax({
                url: "http://localhost:8080/Application2_Web_exploded/customer?id="+ id + "&name=" + name + "&phone=" + contactNo + "&address=" + address,
                method: "PUT",
                success: function (response) {
                    showToast('success', 'Customer updated successfully !');
                    resetForm('#update-customer', '#update-customer input, #update-customer textarea');
                    loadCustomerDatabase();
                },
                error: function (error) {
                    console.log(error)
                }
            });
        } else {
            showToast('error', 'Customer ID not found !');
        }
    }
});

// Search Customer
$('#search-customer').on('submit', function (event) {
    event.preventDefault();

    let value = $('#txt-search-valuec').val();
    let option = $('#searchCustomerBy').val(); // Ensure this matches the options

    console.log("Search option:", option);  // Log the selected option
    console.log("Search value:", value);   // Log the search value

    if (value.trim() !== "") {
        $('#customer-tbody').empty();  // Clear the table

        const customers = getCustomerByOption(option, value);

        console.log("Found customers:", customers);  // Log the found customers

        if (Array.isArray(customers) && customers.length > 0) {
            customers.forEach(customer => appendToCustomerTable(customer));
            showToast('success', 'Customer search completed successfully!');
        } else if (customers && !Array.isArray(customers)) {
            appendToCustomerTable(customers);
            showToast('success', 'Customer search completed successfully!');
        } else {
            showToast('error', `Customer ${option} not found!`);
        }
    } else {
        showToast('error', 'Please enter a valid search value!');
    }
});

// Clear Customer
$('#clear-customer-btn').on('click', function () {
    $('#txt-search-valuec').val('');
    $('#txt-search-valuec').removeClass('is-invalid is-valid');
    $('#txt-search-valuec').next().hide();
    $('#search-customer-by').val('ID');
    $('#customer-tbody').empty();
    showToast('success', 'Customer page cleared !');
});

// Delete Customer
$('#delete-customer-btn').on('click', function () {
    const value = $('#txt-search-valuec').val();
    const option = $('#searchCustomerBy').val(); // Ensure correct ID for dropdown

    // Retrieve customers based on the search criteria
    const customers = getCustomerByOption(option, value);

    if (Array.isArray(customers) && customers.length > 0) {
        // Handle multiple customers case
        confirmAndDelete(customers);
    } else if (customers && !Array.isArray(customers)) {
        // Handle a single customer case
        confirmAndDelete([customers]);
    } else {
        showToast('error', `Customer ${option} not found!`);
        loadAllCustomers();
    }
});

// Function to confirm and delete customers
function confirmAndDelete(customers) {
    // Prepare the confirmation modal message
    $('#confirm-delete-model .modal-body').text(
        customers.length > 1
            ? `Are you sure you want to delete these ${customers.length} customers?`
            : `Are you sure you want to delete the customer "${customers[0].name}"?`
    );

    // Show the confirmation modal
    $('#confirm-delete-model').modal('show');

    // Attach event listener for confirming the delete
    $('#confirm-delete-btn').one('click', function () {
        customers.forEach(customer => {
            $.ajax({
                url: `http://localhost:8080/Application2_Web_exploded/customer?id=${customer.id}`,
                method: 'DELETE',
                success: function () {
                    showToast('success', `Customer "${customer.name}" deleted successfully!`);
                },
                error: function (error) {
                    console.error('Error deleting customer:', error);
                    showToast('error', `Failed to delete customer "${customer.name}".`);
                },
                complete: function () {
                    // Refresh the customer database and UI after deletion
                    loadCustomerDatabase();
                    loadCustomerCount();
                    initializeOrderComboBoxes();
                }
            });
        });

        // Hide the confirmation modal after all deletions are initiated
        $('#confirm-delete-model').modal('hide');
    });
}


// Load All Customers
$('#load-all-customer-btn').on('click', function () {
    loadAllCustomers();
    showToast('success', 'All customers loaded successfully !');
});