// ===== Prevent goin bcak =====
window.history.pushState(null, null, window.location.href);
window.onpopstate = function () {
    window.history.go(1);
};

$(document).ajaxError(function (event, jqxhr, settings, thrownError) {
    if (jqxhr.status === 403) {
        window.location.href = "/403.html";
    }
});


// ===== Reusable API Function =====
function apiRequest(url, data, method = "POST") {
    return fetch(url, {
        method,
        credentials: "include",
        headers: {

            "Content-Type": "application/json"
        },
        body: JSON.stringify(data)
    })
        .then(response => {
            return response.json()
                .then(result => {
                    if (!response.ok) {
                        throw {
                            success: false,
                            status: response.status,
                            message: result.message || "API error",
                            data: result
                        };
                    }
                    return {
                        success: true,
                        status: response.status,
                        message: result.message,
                        data: result
                    };
                });
        });
}

function getApiData(url) {
    return fetch(url, {
        credentials: "include"
    })
        .then(response => {
            if (response.status === 403) {
                window.location.href = "/403.html";
                return;
            }
            if (!response.ok) {
                // console.log(response)
                throw new Error("Failed to fetch data");
            }
            return response.json();
        })
        .catch(error => {
            console.error("API Error:", error.message);
            throw error;
        });
}

function isAuthenticated() {
    return localStorage.getItem("userId") && localStorage.getItem("role");
}

function logout() {
    localStorage.clear();
    window.location.href = "/Html/Login.html";
}
function checkAuthAndRedirect(id = null, roleValue = null) {
    const baseUrl = window.location.origin + "/Html/";

    if (id && roleValue) {
        localStorage.setItem("userId", id);
        localStorage.setItem("role", roleValue);

        window.location.href = `${baseUrl}Dashboard/Dashboard.html`;
    } else {
        const userId = localStorage.getItem("userId");
        const role = localStorage.getItem("role");

        if (!userId || !role) {
            window.location.href = `${baseUrl}Login.html`;
        }
    }
}


// format date
function formatCustomDate(dateArray, forInput = false) {
    if (!Array.isArray(dateArray) || dateArray.length < 3) {
        return 'Invalid date';
    }
    const year = dateArray[0];
    const month = dateArray[1] - 1;
    const day = dateArray[2];
    const hour = dateArray[3] || 0;
    const minute = dateArray[4] || 0;
    const second = dateArray[5] || 0;
    const millis = dateArray[6] ? Math.floor(dateArray[6] / 1e6) : 0;

    const date = new Date(year, month, day, hour, minute, second, millis);

    if (forInput) {
        const yyyy = date.getFullYear();
        const mm = String(date.getMonth() + 1).padStart(2, '0');
        const dd = String(date.getDate()).padStart(2, '0');
        const hh = String(date.getHours()).padStart(2, '0');
        const min = String(date.getMinutes()).padStart(2, '0');
        return `${yyyy}-${mm}-${dd}T${hh}:${min}`;
    } else {
        const options = { day: 'numeric', month: 'long', year: 'numeric' };
        return date.toLocaleDateString('en-US', options);
    }
}

function showAlert(containerId, message, type = 'danger') {
    const alertHTML = `
        <div class="alert alert-${type} alert-dismissible fade show" role="alert">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>`;
    $(`#${containerId}`).html(alertHTML);
}

$(document).ready(function () {

    const page = window.location.pathname;
    const params = new URLSearchParams(window.location.search);
    const publicPages = ["/Html/Login.html", "/Html/SignUP.html", "/Html/Forgotpassword.html", "/Html/ResetPassword.html"];
    const isPublicPage = publicPages.some(path => page.includes(path));
    const userId = localStorage.getItem("userId");
    const role = localStorage.getItem("role");

    if (!userId || !role) {
        if (!isPublicPage) {
            window.location.href = "/Html/Login.html";
        }
    } else {
        if (isPublicPage) {
            window.location.href = "/Html/Dashboard/Dashboard.html";
        }
    }


    //    (both for admin and user)
    function renderCommonMenuItems() {
        return `
                <a href="./Dashboard.html" class="nav-item">
                    <i class="fas fa-home nav-icon"></i> Dashboard
                </a>
            `;
    }

    // admin specific menu items
    function renderAdminMenuItems() {
        return `
         <a href="./AllAdminCreatedTask.html" class="nav-item">
                    <i class="fa fa-tasks nav-icon"></i> Assign Tasks
                </a>
                <a href="./EmployeeList.html" class="nav-item">
                    <i class="fa fa-users nav-icon"></i> Employees List
                </a>
                 <a href="./Review&Approvetask.html" class="nav-item">
                    <i class="fa fa-book nav-icon"></i> Review & Approval
                </a>
                
                 <a href="./Category.html" class="nav-item">
                    <i class="fas fa-bell nav-icon"></i> Category
                </a>
                 <a href="./Profile.html" class="nav-item">
                    <i class="fas fa-user nav-icon"></i> Profile
                </a>
               
                <a href="#" class="nav-item" id="logout">
                    <i class="fas fa-sign-out nav-icon"></i> Logout
                </a>
            `;
    }

    function renderUserMenuItems() {
        return `
                <a href="./AllTasks.html" class="nav-item">
                    <i class="fa fa-tasks nav-icon"></i> My Tasks
                </a>
                 <a href="./CompApp.html" class="nav-item">
                    <i class="fa fa-book nav-icon"></i> Review & Approval
                </a>
                <a href="./OverdueTasks.html" class="nav-item">
                    <i class="fa fa-calendar-times nav-icon"></i> Overdue Tasks
                </a>
                 <a href="./RedoTasks.html" class="nav-item">
                    <i class="fa fa-redo nav-icon"></i> Redo Tasks
                </a>
                 <a href="./Profile.html" class="nav-item">
                    <i class="fas fa-user nav-icon"></i> Profile
                </a>
               
                <a href="#" class="nav-item" id="logout">
                    <i class="fas fa-sign-out nav-icon"></i> Logout
                </a>
            `;
    }


    function renderSidebarMenu() {
        const role = localStorage.getItem("role");
        let menuItems = renderCommonMenuItems();

        if (role === "admin") {
            menuItems += renderAdminMenuItems();
        } else if (role === "user") {
            menuItems += renderUserMenuItems();
        } else {
            menuItems += "<p class='error'>No role found in localStorage</p>";
        }

        $('#nav-menu').html(menuItems);
    }

    renderSidebarMenu();
    const pagepath = window.location.pathname.split('/').pop();
    $('.nav-item').removeClass('active').each(function () {
        const link = $(this).attr('href');
        if (!link) return;

        if (link.includes(pagepath)) $(this).addClass('active');

        if (pagepath === "FormTask.html") {
            if ((params.has('editTaskId') || (!params.has('editTaskId') && !params.has('editDoneTaskId'))) && link.includes("AllTasks.html")) {
                $(this).addClass('active');
            }
            if (params.has('editDoneTaskId') && link.includes("CompApp.html")) {
                $(this).addClass('active');
            }
        }
    });


    // ===== SignUp Form =====
    $("#signupForm").on("submit", function (e) {
        e.preventDefault();

        const firstname = $("input[name='firstname']").val().trim();
        const lastname = $("input[name='lastname']").val().trim();
        const email = $("input[name='email']").val().trim();
        const password = $("input[name='password']").val();
        const confirmpass = $("input[name='confirmpass']").val();

        const hasScript = /<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi;
        const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

        if (!firstname || !lastname || !email || !password || !confirmpass) {
            showAlert("signupAlert", "All fields are required.");
            return;
        }

        if (hasScript.test(firstname) || hasScript.test(lastname) || hasScript.test(email)) {
            showAlert("signupAlert", "Invalid input: Script tag detected.");
            return;
        }

        if (!emailPattern.test(email)) {
            showAlert("signupAlert", "Invalid email format.");
            return;
        }

        if (password.length < 6) {
            showAlert("signupAlert", "Password must be at least 6 characters.");
            return;
        }

        if (password !== confirmpass) {
            showAlert("signupAlert", "Passwords do not match.");
            return;
        }

        const user = {
            firstname,
            lastname,
            email,
            password
        };
        // api call to save data in db 
        apiRequest("http://localhost:8080/register", user)
            .then(res => {
                // console.log(res.data);
                checkAuthAndRedirect(res.data.userId, res.data.role)
                showAlert("signupAlert", res.message, "success")
            })
            .catch(err => {
                showAlert("signupAlert", err.message);
            });
    });

    // ===== Login Form =====
    $("#loginForm").on("submit", function (e) {
        e.preventDefault();

        const email = $("input[name='login_email']").val().trim();
        const password = $("input[name='login_password']").val();

        const hasScript = /<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi;
        const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

        if (!email || !password) {
            showAlert("loginAlert", "Both fields are required.");
            return;
        }

        if (hasScript.test(email)) {
            showAlert("loginAlert", "Invalid input: Script tag detected.");
            return;
        }

        if (!emailPattern.test(email)) {
            showAlert("loginAlert", "Invalid email format.");
            return;
        }

        if (password.length < 6) {
            showAlert("loginAlert", "Password must be at least 6 characters.");
            return;
        }

        const loginData = {
            email,
            password
        };

        // api call for login 
        apiRequest("http://localhost:8080/login", loginData)
            .then(res => {
                checkAuthAndRedirect(res.data.userId, res.data.role)
                showAlert("loginAlert", res.message, "success")
            })
            .catch(err => {
                showAlert("loginAlert", err.message);
            });
    });

    // ===== ForgotPassword ====
    $("#forgotForm").on("submit", function (e) {
        e.preventDefault();
        // const host = location.hostname;
        const host = location.origin + "/Html"
        const email = $("input[name='forgot_email']").val().trim();
        const hasScript = /<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi;
        const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

        if (!email) {
            showAlert("forgotpasswordAlert", "Email is required.");
            return;
        }

        if (hasScript.test(email)) {
            showAlert("forgotpasswordAlert", "Invalid input: Script tag detected.");
            return;
        }

        if (!emailPattern.test(email)) {
            showAlert("forgotpasswordAlert", "Invalid email format.");
            return;
        }
        const forgotEmail = {
            email,
            host
        };
        apiRequest("http://localhost:8080/forgot-password", forgotEmail)
            .then(res => {
                showAlert("forgotpasswordAlert", res.message, "success")
            })
            .catch(err => {
                showAlert("forgotpasswordAlert", err.message);
            });
    });

    // ===== Reset Password ====
    $("#resetPassword").on("submit", function (e) {
        e.preventDefault();

        const urlParams = new URLSearchParams(window.location.search);
        const token = urlParams.get('token');
        const password = $("input[name='newpassword']").val();
        const confirmpass = $("input[name='confirmpass']").val();

        const hasScript = /<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi;

        if (!password || !confirmpass) {
            showAlert("resetAlert", "Both fields are required.");
            return;
        }

        if (hasScript.test(password) || hasScript.test(confirmpass)) {
            showAlert("resetAlert", "Invalid input: Script tag detected.");
            return;
        }

        if (password.length < 6 || password.length > 12) {
            showAlert("resetAlert", "Password must be between 6 and 12 characters.");
            return;
        }

        if (password !== confirmpass) {
            showAlert("resetAlert", "Passwords do not match.");
            return;
        }

        const resetpassData = {
            newpassword: password,
            token: token
        };
        apiRequest("http://localhost:8080/reset-password", resetpassData)
            .then(res => {
                showAlert("resetAlert", res.message, "success")
            })
            .catch(err => {
                showAlert("resetAlert", err.message);
            });
    });

    // Categories
    if (page.includes("Category.html") && role.toLowerCase() == "admin")
        loadCategories();

    // add and edit category
    $("#submit_cat").on("click", function (e) {
        e.preventDefault();
        const catId = $("#cat_id").val(),
            catName = $("input[name='categoryName']").val(),
            catDesc = $("textarea[name='categoryDescription']").val();

        if (!catName) return showAlert("categoryAlert", "Category Name is required.");

        const scriptTag = /<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi;
        if (scriptTag.test(catName) || scriptTag.test(catDesc)) {
            return showAlert("categoryAlert", "Invalid input: Script tag detected.");
        }

        const data = { categoryName: catName, categoryDescription: catDesc };
        const url = catId
            ? `http://localhost:8080/admin/categories/update-category/${catId}`
            : `http://localhost:8080/admin/categories/add-category`;

        apiRequest(url, data).then(res => {
            showAlert("categoryAlert", res.message, "success");
            setTimeout(() => {
                $("#categoryModal").modal("hide");
                resetCategoryModal();
                loadCategories();
            }, 1000);
        }).catch(err => showAlert("categoryAlert", err.message));
    });

    // load and list category
    function loadCategories() {
        getApiData(`http://localhost:8080/admin/categories/get-all`).then(
            renderCategoryTable
        ).catch(
            console.error
        );
    }
    // render category on table
    function renderCategoryTable(categories) {
        const tbody = $("table.application-table tbody").empty();
        if (!categories.length) return tbody.append(`<tr><td colspan="3" class="text-center">No categories found.</td></tr>`);

        categories.forEach((cat, i) => {
            tbody.append(`
                    <tr>
                        <td>
                            <div class="application-company">
                                <div class="company-logo">${i + 1}</div>
                                <div><div class="company-name">${cat.categoryName}</div></div>
                            </div>
                        </td>
                        <td>${cat.categoryDescription || "--"}</td>
                        <td>
                            <table><tr>
                                <td><button class="action-btn edit-btn" data-id="${cat.catId || cat.cat_id}" data-name="${cat.categoryName}" data-desc="${cat.categoryDescription || ''}"><i class="fa fa-pencil-square"></i></button></td>
                                <td><button class="action-btn delete-btn" data-id="${cat.catId || cat.cat_id}"><i class="fa fa-trash"></i></button></td>
                            </tr></table>
                        </td>
                    </tr>
                `);
        });
    }

    // Display data on modal(edit category)
    $(document).on("click", ".edit-btn", function () {
        $("#cat_id").val($(this).data("id"));
        $("#categoryName").val($(this).data("name"));
        $("#categoryDescription").val($(this).data("desc"));
        $("#categoryModalLabel").text("Edit Category");
        $("#categoryModal").modal("show");
    });

    // reset modal
    $("#categoryModal").on("hidden.bs.modal", resetCategoryModal);

    function resetCategoryModal() {
        $("#category_form")[0].reset();
        $("#cat_id").val("");
        $("#categoryModalLabel").text("Add New Category");
        $("#categoryAlert").html("");
    }

    // deleting category
    $(document).on("click", ".delete-btn", function () {
        const catId = $(this).data("id");
        if (confirm("Are you sure you want to delete this category?")) {
            apiRequest(`http://localhost:8080/admin/categories/delete-category/${catId}`, {}, 'DELETE')
                .then(res => {
                    showAlert("categoryAlert", res.message, "success");
                    loadCategories();
                }).catch(err => showAlert("categoryAlert", err.message));
        }
    });

    // seacrhc and list category
    function searchCategories() {
        const keyword = $("#cat-search-bar input").val().trim();
        if (!keyword) return loadCategories();

        getApiData(`http://localhost:8080/admin/categories/search?keyword=${encodeURIComponent(keyword)}`)
            .then(renderCategoryTable)
            .catch(err => console.error("Search error:", err));
    }

    $("#search_category").on("click", searchCategories);
    $("#input_keyword").on("keyup", function (e) {
        if (e.key === "Enter") searchCategories();
    });


    // profile page
    let originalImageURL = "";
    let defaultImage = "../../images/avatar.png";

    if (page.includes("Profile.html")) {
        loadUserProfile();
    }
    // Load profile data
    function loadUserProfile() {
        const id = localStorage.getItem("userId");
        const role = localStorage.getItem("role");
        if (role && role?.toUpperCase() == "ADMIN") {
            $("#fullnamecontainer").show();
            $("#firstlastnameContainer").remove();
        }
        else {
            $("#fullnamecontainer").remove();
            $("#firstlastnameContainer").show();
        }
        fetch(`http://localhost:8080/user/profile/${id}`, {
            method: "POST",
            credentials: "include"
        })
            .then(res => res.json())
            .then(res => {
                if (res.status === "success") {
                    const user = res.data;
                    // console.log(user.profileImg);
                    $("#welcome_name").text("Welcome " + (user.firstname ? user.firstname : user.fullname) + " !")
                    $("#firstname").val(user.firstname);
                    $("#fullname").val(user.fullname);
                    $("#lastname").val(user.lastname);
                    $("#email").val(user.email);
                    $("#phonenumber").val(user.phonenumber);

                    originalImageURL = (user.profileImg && user.profileImg.length > 30)
                        ? user.profileImg
                        : defaultImage;
                    // console.log(originalImageURL)
                    $("#profileImgPreview").attr("src", originalImageURL).show();
                } else {
                    showAlert("profileAlert", "Failed to load profile");
                }
            })
            .catch(err => console.log("Error loading profile: " + err));
    }

    // Submit profile form with FormData
    $("#profile_form").on("submit", function (e) {
        e.preventDefault();
        const id = localStorage.getItem("userId");
        const formData = new FormData(this);

        fetch(`http://localhost:8080/${role}/uploadProfileImage/${id}`, {
            method: "POST",
            body: formData,
            credentials: "include"
        })
            .then(res => res.json())
            .then(res => {
                showAlert("profileAlert", res.message, "success");
                loadUserProfile();
            })
            .catch(err => showAlert("profileAlert", "Something went wrong"));
    });

    // Preview image on change
    $("#profileImg").on("change", function () {
        const file = this.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = e => {
                $("#profileImgPreview").attr("src", e.target.result);
            };
            reader.readAsDataURL(file);
        } else {
            $("#profileImgPreview").attr("src", originalImageURL || defaultImage);
        }
    });

    // Remove image preview
    $("#removeImageBtn").on("click", function () {
        $("#profileImg").val("");
        $("#profileImgPreview").attr("src", defaultImage);
    });


    //Change pass word from profile
    $("#ChangePass").on("submit", function (e) {
        e.preventDefault(); // Prevent default form submission

        const id = localStorage.getItem("userId");
        const oldPass = $("input[name='oldpass']").val();
        const newPass = $("input[name='newpassword']").val();
        const confirmPass = $("input[name='confirmPass']").val();

        const hasScript = /<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi;

        if (!oldPass || !newPass || !confirmPass) {
            showAlert("changePassAlert", "Please fill all fields");
            return;
        }

        if (newPass !== confirmPass) {
            showAlert("changePassAlert", "Passwords do not match");
            return;
        }

        if (hasScript.test(newPass) || hasScript.test(confirmPass) || hasScript.test(oldPass)) {
            showAlert("changePassAlert", "Invalid input: Script tag detected.");
            return;
        }

        if (newPass.length < 6 || newPass.length > 12) {
            showAlert("changePassAlert", "Password must be between 6 and 12 characters.");
            return;
        }

        data = {
            "oldpassword": oldPass,
            "newpassword": newPass
        }
        apiRequest(`http://localhost:8080/user=${id}/change-password`, data)
            .then(res => {
                showAlert("changePassAlert", res.message, "success");
                $("#ChangePass")[0].reset();
                logout();
            })
            .catch(err => {
                showAlert("changePassAlert", err.message);
            });
    });

    // // userdashboard
    if (page.includes("Dashboard.html") && role?.toLowerCase() === "user") {
        $("#employeeSection").addClass("d-none");
        $("#priorityTasksSection").removeClass("d-none");

        const loggedUserId = localStorage.getItem("userId");
       const link = document.getElementById("viewAllLink");
        const redoLink =  document.getElementById("viewRedoLink");

        link.href = "./AllTasks.html";
        redoLink.href = "./RedoTasks.html"
        getApiData(`http://localhost:8080/taskify/user/dashboard?loggedUserId=${loggedUserId}`)
            .then(res => {
                if (res.status === "error") {
                    console.error(res.message);
                    return;
                }

                fillStats(res);
                fillPriorityTasks(res.highPriorityTasks);
                fillTodoInProgressTasks(res.todoInprogressTasks);
                fillRedoTasks(res.redoHighPriorityTasks);
            })
            .catch(err => {
                console.error("Dashboard load error:", err);
            });
    }

    function fillStats(data) {
        // console.log(data)
        $("#totalTasks").text(data.totalTasks || data.totalTasksAssigned);
        $("#incompleteTasks").text(data.incompleteTasks || data.todoAndInprogressTasks);
        $("#overdueTasks").text(data.overdueTasks);
        $("#pendingApprovalTasks").text(data.pendingApprovalTasks);
    }

    function fillPriorityTasks(tasks) {
        const container = $("#priorityTaskList");
        container.empty();

        tasks.forEach(task => {
            const html = `
            <div class="job-card">
                <div class="job-logo"><i class="fas fa-bolt text-danger"></i></div>
                <div class="job-info">
                    <div class="job-company">${task.category}</div>
                    <h3 class="job-title">${task.title}</h3>
                    <div class="job-meta">
                        <span><i class="fas fa-clock"></i> ${task.status}</span>
                        <span><i class="fas fa-flag"></i> ${task.priority}</span>
                        <span><i class="fas fa-calendar-alt"></i> ${formatCustomDate(task.dueDate)}</span>
                    </div>
                </div>
                <div class="job-tag tag-new">${task.priority}</div>
            </div>`;
            container.append(html);
        });
    }

    function fillTodoInProgressTasks(tasks) {
        console.log(tasks)
        const table = $("#myTaskTableBody");
        table.empty();

        tasks.forEach(task => {
            const row = `
            <tr>
                <td>
                    <div class="application-company">
                        <div class="company-logo"><i class="fas fa-laptop-code"></i></div>
                        <div>
                            <div class="company-name">${task.title}</div>
                            <div class="company-position">${task.category}</div>
                        </div>
                    </div>
                </td>
                <td>${formatCustomDate(task.createdTime)}</td>
                <td>
                    <div class="application-status status-${task.status.toLowerCase()}">
                        ${task.status}
                    </div>
                </td>
            </tr>`;
            table.append(row);
        });
    }

    function fillRedoTasks(tasks) {
        const container = $("#redoTaskList");
        container.empty();

        tasks.forEach(task => {
            const html = `
                    <div class="application-company" style="padding-top:10px">
                        <div class="activity-icon">
                            <i class="fas fa-redo text-danger"></i>
                        </div>
                        <div>
                            <div class="activity-title">${task.title} (${task.category})</div>
                            <div class="activity-time">Due: ${formatCustomDate(task.dueDate)}</div>
                        </div>
                    </div>
                `;
            container.append(html);
        });
    }

    if (page.includes("Dashboard.html") && role?.toLowerCase() === "admin") {

        $("#priorityTasksSection").addClass("d-none");
        $("#employeeSection").removeClass("d-none");

        const loggedUserId = localStorage.getItem("userId");

        const link = document.getElementById("viewAllLink");
        const redoLink =  document.getElementById("viewRedoLink");
        link.href = "./AllAdminCreatedTask.html";
        redoLink.href = "./AllAdminCreatedTask.html"
        getApiData(`http://localhost:8080/taskify/admin/dashboard?adminId=${loggedUserId}`)
            .then(res => {
                if (res.status === "error") {
                    console.error(res.message);
                    return;
                }

                fillStats(res);
                fillEmployees(res.latestEmployees);
                fillTodoInProgressTasks(res.latestAssignedTasks);
                fillRedoTasks(res.redoTasks);
            })
            .catch(err => {
                console.error("Dashboard load error:", err);
            });
    }

    function fillEmployees(empList) {
        console.log(empList)
        const container = $("#employeeList");
        container.empty();

        empList.forEach(task => {
            const html = `
            <div class="job-card">
                <div class="job-logo"><i class="fas fa-bolt text-danger"></i></div>
                <div class="job-info">
                    <div class="job-company">${task.email}</div>
                    <h3 class="job-title">${task.firstname} ${task.lastname}</h3>
                    <div class="job-meta">
                        <span><i class="fas fa-clock"></i> ${formatCustomDate(task.registerDate)}</span>
                        
                        <span><i class="fa fa-phone"></i> ${task.phonenumber}</span>
                    </div>
                </div>
            </div>`;
            container.append(html);
        });
    }
    // });

    // function fetchAndDisplayEmployees() {
    //     $.get("http://localhost:8080/admin/all-employees", function (res) {
    //         if (res.status === "success") {
    //             const employees = res.data;
    //             let html = "";

    //             employees.forEach(emp => {
    //                 html += `
    //                 <div class="job-item">
    //                     <strong>${emp.firstname} ${emp.lastname}</strong>
    //                     <div>Email: ${emp.email}</div>
    //                     <div>Phone: ${emp.phonenumber}</div>
    //                 </div>
    //             `;
    //             });

    //             $("#employeeList").html(html);
    //         } else {
    //             $("#employeeList").html("<p>No employees found.</p>");
    //         }
    //     }).fail(function () {
    //         $("#employeeList").html("<p>Error loading employees.</p>");
    //     });
    // }


    // logout
    $("#logout").on("click", function (e) {
        getApiData("http://localhost:8080/logout")
            .then(res => {
                console.log(res.message)
            })
            .catch(err => {
                console.log(res.message)
                // showAlert("taskAlert", err.message);
            });
        logout();

    })
});
// Job card click handler
const jobCards = document.querySelectorAll(".job-card");

jobCards.forEach((card) => {
    card.addEventListener("click", function () {
        this.style.borderColor = "var(--primary-color)";
        setTimeout(() => {
            this.style.borderColor = "";
        }, 300);
    });
});

