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
            menuItems += "<p class='error'>No role found</p>";
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
            if (params.has('editAssignTask') && link.includes("AllAdminCreatedTask.html")) {
                $(this).addClass('active');
            }

        }
    });

    // Add task user
    if (page.includes("FormTask.html")) {
        const role = localStorage.getItem("role");

        applyRoleBasedUI(role);
        populateCategoryDropdown();

        if (role?.toUpperCase() === "ADMIN") {
            populateUserDropdown();
        }
    }

    function applyRoleBasedUI(role) {
        if (role?.toUpperCase() === "ADMIN") {
            $("#userAssignmentField").show();
            $("#adminAssignmentField").hide();
        } else if (role?.toUpperCase() === "USER") {
            $("#userAssignmentField").hide();
            $("#adminAssignmentField").show();
            $('#status option[value="Completed"], option[value="Redo"], option[value="Overdue"]').remove();
        }
    }
    //populating category 
    function populateCategoryDropdown() {
        getApiData("http://localhost:8080/admin/categories/get-all")
            .then(categories => {
                const dropdown = $("#categoryDropdown");
                dropdown.empty();
                dropdown.append('<option value="">--Select category--</option>');

                categories.forEach(cat => {
                    dropdown.append(`<option value="${cat.categoryName}">${cat.categoryName}</option>`);
                });
            })
            .catch(err => {
                console.error("Failed to load admins list :", err);
            });
    }

    //populating user dropdown
    function populateUserDropdown() {
        getApiData("http://localhost:8080/admin/users-list")
            .then(users => {
                const dropdown = $("#userAssign");
                dropdown.empty();
                dropdown.append('<option value="">--Select User--</option>');

                users.forEach(user => {
                    dropdown.append(`<option value="${user.user_id}" data-email="${user.email}">${user.firstname} ${user.lastname} - (${user.email})</option>`);
                });
            })
            .catch(err => {
                console.error("Failed to load users list: ", err);
            });
    }

    // fetching over due tasks
    if (page.includes("EmployeeList.html") && role == "admin") {
        loadEmployeeList();
    }

    function loadEmployeeList() {
        getApiData(`http://localhost:8080/admin/users-list`)
            .then(empList => {
                renderempListontable(empList.data, "employee-list");
            })
            .catch(err => {
                showAlert("taskAlert", err.message);
            });

    }


    function renderempListontable(empList, tableBodyId) {
        console.log(empList)
        const $tableBody = $(`#${tableBodyId}`);
        $tableBody.empty();
        if (!empList.length) return tbody.append(`<tr><td colspan="3" class="text-center">No Employees Found.</td></tr>`);

        empList.forEach((emp, index) => {
            const row = `
        <tr data-taskId='${emp.userId}'>
            <td>
                <div class="application-company">
                    <div class="company-logo">#${emp.userId}</div>
                    <div>
                        <div class="company-name">${emp.firstname}  ${emp.lastname} </div>
                    </div>
                </div>
            </td>
            <td>${emp.email}</td>
            <td>${emp.phoneNum?.trim() || emp.phonenumber}</td>
           <td>${formatCustomDate(emp.registerDate)}</td>
            
            <td>
                <table>
                    <tr>
                        <td>
                            <button class="action-btn view-empdetails-btn" data-index="${index}" title="View Employee">
                                <i class="fas fa-eye"></i>
                            </button>
                        </td>
                        
                        <td>
                            <button class="action-btn delete_emp" data-userID="${emp.userId}" title="Delete Employee">
                                <i class="fa fa-trash"></i>
                            </button>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        `;
            $tableBody.append(row);
        });

        // Bind modal view buttons
        $('.view-empdetails-btn').off('click').on('click', function () {
            const index = $(this).data('index');
            const emp = empList[index];

            $('#emp_id').html("<strong>ID : </strong>" + emp.userId);
            $('#emp_name').text(emp.firstname + " " + emp.lastname);
            $('#emp_email').html("<strong>Email : </strong>" + emp.email);
            $('#emp_phn_num').html("<strong>Phone Number : </strong>" + emp.phoneNum);
            $('#emp_regDate').html("<strong>Joined At : </strong>" + formatCustomDate(emp.registerDate));

            let Imagesrc;
            if ((emp.profileimg && typeof emp.profileimg === 'string' && emp.profileimg.trim() !== "") || emp.profileImg) {
                Imagesrc = "data:image/jpeg;base64," + (emp.profileimg || emp.profileImg);
            } else {
                Imagesrc = "../../Images/avatar.png";
            }

            $('.employee-image img').attr('src', Imagesrc);

            const modal = new bootstrap.Modal(document.getElementById('viewemployeeModal'));
            modal.show();
        });
    }

    if (page.includes("AllAdminCreatedTask.html") && role == "admin") {
        populateUserDropdown("alltask_sort-by-emp");
        loadAdminAssignedTask();
    }
    if (page.includes("Review&Approvetask.html") && role == "admin") {
        populateUserDropdown("review-sort-by-emp");
        populateUserDropdown("approve-sort-by-emp");
    }
    // ppulating user email on filter all task
    function populateUserDropdown(dropdownId) {
        getApiData("http://localhost:8080/admin/users-list")
            .then(users => {
                const dropdown = $(`#${dropdownId}`);
                dropdown.empty();
                users.data.forEach(user => {
                    dropdown.append(`
                <li><a class="dropdown-item filter-by-email" data-email="${user.email}" data-empId="${user.userId}">${user.email}</a></li>
            `);
                });
            })
            .catch(err => {
                console.error("Failed to load users list: ", err);
            });
    }

    // render task 
    function renderTasksontable(tasks, tableBodyId, edit_btn_id) {
        const $tableBody = $(`#${tableBodyId}`);
        $tableBody.empty();

        // console.log(tasks);
        if (!tasks.length) return $tableBody.append(`<tr><td colspan="6" class="text-center">No Tasks Found.</td></tr>`);


        tasks.forEach((task, index) => {
            const row = `
            <tr data-taskId='${task.taskId}'>
            <td>
                <div class="application-company">
                <div class="company-logo">${index + 1}</div>
                <div>
                <div class="company-name">${task.title}</div>
                        <div class="company-position">${task.category}</div>
                        </div>
                        </div>
                        </td>
            <td>${task.user.email}</td>
            <td>
            <div class="application-status status-${task.status.toLowerCase().replace(/\s/g, '-')}">
            ${task.status}
            </div>
            </td>
            <td>
            <span class="badge priority-badge-${task.priority.toLowerCase()}">${task.priority}</span>
            </td>
            <td>${formatCustomDate(task.createdTime)}</td>
            <td>
            <table>
            <tr>
            <td>
            <button class="action-btn view-task-btn" data-index="${index}" title="View Task">
            <i class="fas fa-eye"></i>
            </button>
            </td>
            <td>
            <button class="action-btn ${edit_btn_id}" data-taskid="${task.taskId}" title="Edit Task">
            <i class="fa fa-pencil-square"></i>
            </button>
            </td>
            <td>
            <button class="action-btn delete_assigned_task" data-taskID="${task.taskId}" title="Delete Task">
            <i class="fa fa-trash"></i>
            </button>
            </td>
            </tr>
            </table>
            </td>
            </tr>
            `;
            $tableBody.append(row);
        });

        // Bind modal view buttons
        $('.view-task-btn').off('click').on('click', function () {
            const index = $(this).data('index');
            const task = tasks[index];

            $('#modal-task-assignedToo').html("Assigned To : " + task.user.email)
            $('#modal-task-title').text(task.title);
            $('#modal-task-description').text(task.description || '—');
            $('#modal-task-status').text(task.status);
            $('#modal-task-priority').text(task.priority);
            $('#modal-task-category').text(task.category || '—');
            $('#modal-task-created').text(formatCustomDate(task.createdTime));
            $('#modal-task-due').text(formatCustomDate(task.dueDate));
            $('#modal-task-userRemark').text(task.userRemark || '—');
            $('#modal-task-adminRemark').text(task.adminRemark || '—');

            const modal = new bootstrap.Modal(document.getElementById('viewAdminTaskModal'));
            modal.show();
        });
    }

    // handel delete task user
    $(document).on("click", ".delete_assigned_task", function () {
        const taskid = $(this).attr('data-taskID');

        if (confirm("Are you sure you want to delete this task ? Deleting this task will delete the task for user also.")) {
            apiRequest(`http://localhost:8080/tasks/delete/${taskid}`, "", 'DELETE')
                .then(res => {
                    showAlert("taskviewalert", res.message, "success");
                    $("#taskviewalert").html("");
                    loadUserTask();
                })
                .catch(err => {
                    showAlert("taskviewalert", err.message);
                });
        }
    });

    
    // handel delete task user
    $(document).on("click", ".delete_emp", function () {
        const userID = $(this).attr('data-userID');

        if (confirm("Are you sure you want to delete this user ? Deleting this user will delete all the task of this user also.")) {
            apiRequest(`http://localhost:8080/admin/user-delete/${userID}`, "", 'DELETE')
                .then(res => {
                    console.log(res.message)
                    loadEmployeeList();
                })
                .catch(err => {
                    console.log(err.message);
                });
        }
    });


    function searchassignTask() {
        const keyword = $("#assign-search-bar input").val().trim();
        if (!keyword) return loadAdminAssignedTask();
        const adminId = localStorage.getItem("userId");
        getApiData(`http://localhost:8080/admin/assigned-tasks-search?keyword=${encodeURIComponent(keyword)}&adminId=${encodeURIComponent(adminId)}`)
            .then(response => {
                renderTasksontable(response.data, "adminAllTasks", "adminAllTasksEdit-btn")
            })
            .catch(err =>
                console.error("Search error:", err));
    }

    $("#search_assignedtask_btn").on("click", searchassignTask);
    $("#search_assignedtask_input").on("keyup", function (e) {
        if (e.key === "Enter") searchassignTask();
    });

    // load admin created task
    function loadAdminAssignedTask() {
        const id = localStorage.getItem("userId");
        getApiData(`http://localhost:8080/admin/assigned-tasks/admin=${id}`)
            .then(tasks => {
                renderTasksontable(tasks.data, "adminAllTasks", "adminAllTasksEdit-btn");
            })
            .catch(err => {
                showAlert("taskAlert", err.message);
            });
    }

    // handle edit task by admin at assign
    $(document).on('click', ".adminAllTasksEdit-btn", function () {
        const taskId = $(this).data('taskid');
        window.location.href = `./FormTask.html?editAssignTask=${taskId}`;
    })

    // sorting
    $("#assigned-sort-options .dropdown-item").on("click", function () {
        let selectedSort = $(this).data("sort");
        fetchFilteredTasks("by-sort", `sort=${encodeURIComponent(selectedSort)}`, "assigned");
    });
    // sort by priority
    $("#assigned-priority-options .dropdown-item").on("click", function () {
        let selectedPrioritiy = $(this).data("priority");
        fetchFilteredTasks("by-priority", `priority=${encodeURIComponent(selectedPrioritiy)}`, "assigned");
    });

    // filter-by-status
    $("#assigned-status-options .dropdown-item").on("click", function () {
        let selectedstatus = $(this).data("status");
        fetchFilteredTasks("by-status", `status=${encodeURIComponent(selectedstatus)}`, "assigned");
    });

    $("#alltask_sort-by-emp").on("click", ".filter-by-email", function () {
        let selectedemail = $(this).data("email");
        let selectedEmpid = $(this).data("empid");
        // console.log(selectedEmpid, selectedemail);
        fetchFilteredTasks("by-email", `email=${encodeURIComponent(selectedemail)}&empId=${encodeURIComponent(selectedEmpid)}`, "assigned");
    });


    function fetchFilteredTasks(method, selectedOption, tasktype) {
        getApiData(`http://localhost:8080/admin/filter-${method}?&taskStatus=${tasktype}&${selectedOption}`)
            .then(response => {
                // console.log(response)
                if (response.message) {
                    if (tasktype == "assigned") {
                        showAlert("AssignedTaskMessage", response.message);
                    } else if (tasktype == "done") {
                        showAlert("EmpDoneAlertMsg", response.message)
                    } else if (tasktype == "completed") {
                        showAlert("EmpApprovedAlertMsg", response.message)
                    }
                } else {

                    $("#AssignedTaskMessage").text("");
                    $("#EmpDoneAlertMsg").text("");
                    $("#EmpApprovedAlertMsg").text("")
                    let tasks = response.data;
                    if (tasktype == "assigned") {
                        renderTasksontable(tasks, "adminAllTasks", "adminAllTasksEdit-btn");
                    } else if (tasktype == "done") {
                        renderPendingApprovedTasksontable(tasks, "empDoneTasks", "reviewtaskEdit-btn");
                    } else if (tasktype == "completed") {
                        renderPendingApprovedTasksontable(tasks, "approvedTasks", "reviewtaskEdit-btn", "hide-edit");
                    }
                }
            })
            .catch(error => {
                console.error(error);
            });
    }

    // Review & Approval page
    // sorting
    $("#wait-sort-options .dropdown-item").on("click", function () {
        let selectedSort = $(this).data("sort");
        fetchFilteredTasks("by-sort", `sort=${encodeURIComponent(selectedSort)}`, "done");
    });
    $("#approved-sort-options .dropdown-item").on("click", function () {
        let selectedSort = $(this).data("sort");
        fetchFilteredTasks("by-sort", `sort=${encodeURIComponent(selectedSort)}`, "completed");
    });
    // sort by priority
    $("#wait-priority-options .dropdown-item").on("click", function () {
        let selectedPrioritiy = $(this).data("priority");
        fetchFilteredTasks("by-priority", `priority=${encodeURIComponent(selectedPrioritiy)}`, "done");
    });
    $("#approved-priority-options .dropdown-item").on("click", function () {
        let selectedPrioritiy = $(this).data("priority");
        fetchFilteredTasks("by-priority", `priority=${encodeURIComponent(selectedPrioritiy)}`, "completed");
    });
    $("#review-sort-by-emp").on("click", ".filter-by-email", function () {
        let selectedemail = $(this).data("email");
        let selectedEmpid = $(this).data("empid");
        // console.log(selectedEmpid, selectedemail);
        fetchFilteredTasks("by-email", `email=${encodeURIComponent(selectedemail)}&empId=${encodeURIComponent(selectedEmpid)}`, "done");
    });
    $("#approve-sort-by-emp").on("click", ".filter-by-email", function () {
        let selectedemail = $(this).data("email");
        let selectedEmpid = $(this).data("empid");
        // console.log(selectedEmpid, selectedemail);
        fetchFilteredTasks("by-email", `email=${encodeURIComponent(selectedemail)}&empId=${encodeURIComponent(selectedEmpid)}`, "completed");
    });

    // fecth and populated task that are waiting for approval
    if (page.includes("Review&Approvetask.html") && role == "admin") {
        loadDoneORCompletedAdminask("empDone");
    }
    $(document).on('click', '#empDone-tab', function () {
        loadDoneORCompletedAdminask("empDone");
    });

    $(document).on('click', '#approved-tab', function () {
        loadDoneORCompletedAdminask("approved");
    });

    // fetching aproval and pending for approval tasks
    function loadDoneORCompletedAdminask(tabName) {

        // console.log(tabName);
        const id = localStorage.getItem("userId");
        let endpoint = "";

        if (tabName === "empDone") {
            endpoint = `http://localhost:8080/admin/all-pending-for-approval?adminid=${id}`;
        } else if (tabName === "approved") {
            endpoint = `http://localhost:8080/admin/all-approved-tasks?adminid=${id}`;
        }

        getApiData(endpoint)
            .then(tasks => {
                // console.log(tasks);
                const tableId = tabName === "empDone" ? "empDoneTasks" : "approvedTasks";

                renderPendingApprovedTasksontable(tasks.data, tableId, "reviewtaskEdit-btn");
                if (tableId == 'approvedTasks') {
                    renderPendingApprovedTasksontable(tasks.data, tableId, "reviewtaskEdit-btn", "hide-edit");
                }
            })
            .catch(err => {
                showAlert("taskAlert", err.message);
            });
    }

    function renderPendingApprovedTasksontable(tasks, tableBodyId, edit_btn_id, disp_edit = null) {
        const $tableBody = $(`#${tableBodyId}`);
        $tableBody.empty();

        // console.log(tasks);
        if (!tasks.length) return $tableBody.append(`<tr><td colspan="5" class="text-center">No Tasks Found.</td></tr>`);

        tasks.forEach((task, index) => {

            const dispEditBtn = disp_edit != null ? "" : `
            <button class="action-btn ${edit_btn_id}" data-taskid="${task.taskId}" title="Approve Task">
                <i class="fa fa-pencil-square"></i>
            </button>`;

            const row = `
            <tr data-taskId='${task.taskId}'>
            <td>
                <div class="application-company">
                <div class="company-logo">#${task.taskId}</div>
                <div>
                <div class="company-name">${task.title}</div>
                        <div class="company-position">${task.category}</div>
                        </div>
                        </div>
                        </td>
            <td>${task.user.email}</td>
            <td>
            <span class="badge priority-badge-${task.priority.toLowerCase()}">${task.priority}</span>
            </td>
            <td>${formatCustomDate(task.dueDate)}</td>
            <td>
            <table>
            <tr>
            <td>
            <button class="action-btn view-task-btn" data-index="${index}" title="View Task">
            <i class="fas fa-eye"></i>
            </button>
            </td>
            <td>
           ${dispEditBtn}

            </td>
            </tr>
            </table>
            </td>
            </tr>
            `;
            $tableBody.append(row);
        });

        // Bind modal view buttons
        $('.view-task-btn').off('click').on('click', function () {
            const index = $(this).data('index');
            const task = tasks[index];

            $('#modal-task-assignedToo').html("Assigned To : " + task.user.email)
            $('#modal-task-title').text(task.title);
            $('#modal-task-description').text(task.description || '—');
            $('#modal-task-status').text(task.status);
            $('#modal-task-priority').text(task.priority);
            $('#modal-task-category').text(task.category || '—');
            $('#modal-task-created').text(formatCustomDate(task.createdTime));
            $('#modal-task-due').text(formatCustomDate(task.dueDate));
            $('#modal-task-userRemark').text(task.userRemark || '—');
            $('#modal-task-adminRemark').text(task.adminRemark || '—');

            const modal = new bootstrap.Modal(document.getElementById('viewAdminTaskModal'));
            modal.show();
        });

        // approve task buttin clicked
        // Approve Task button click event
        $(`.${edit_btn_id}`).off('click').on('click', function () {
            const taskId = $(this).data('taskid');
            openApproveTaskModal(taskId);
        });
    }

    function openApproveTaskModal(taskId) {
        $('#approveTaskId').val(taskId);
        $('#taskStatus').val('');
        $('#adminRemark').val('');
        $('#approveTaskModal').modal('show');
    }

    $(document).on('click', "#approveDoneTask", function () {
        const id = localStorage.getItem("userId");

        const hasScript = /<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi;
        const taskId = $('#approveTaskId').val();
        const status = $('#taskStatus').val();
        const remarks = $('#adminRemark').val();

        if (status == "" || remarks.trim() == "") {
            showAlert("EmpDoneModalAlertMsg", "Please select a status and enter your remarks");
            return;
        }
        if (hasScript.test(remarks) || hasScript.test(status)) {
            showAlert("EmpDoneModalAlertMsg", "Invalid input: Script tag detected.");
            return;
        }
        const data = {
            "taskId": taskId,
            "status": status,
            "adminRemark": remarks
        }
        // approve-task/task={id}
        apiRequest(`http://localhost:8080/admin/approve-task/taskId=${taskId}`, data)
            .then(res => {
                // console.log(res)
                showAlert("taskviewalert", res.message, "success");
                $("#taskviewalert").html("");
                 $('#approveTaskModal').modal('hide');
                loadAdminAssignedTask();
            })
            .catch(err => {
                showAlert("taskviewalert", err.message);
            });
    })
    $("#approveTaskModal").on("hidden.bs.modal", function () {
        $("#approveTaskForm")[0].reset();
        $("#EmpDoneModalAlertMsg").empty();
    });


    function searchReviewNApprovedTask(tabname) {
        let keyword = "";
        let taskstatus = "";

        if (tabname === "empDone") {
            keyword = $("#waitApp-search-bar input").val().trim();
            taskstatus = "Done";
        } else {
            keyword = $("#approvedApp-search-bar input").val().trim();
            taskstatus = "Completed";
        }

        if (!keyword) return loadDoneORCompletedAdminask(tabname);

        const adminId = localStorage.getItem("userId");

        getApiData(`http://localhost:8080/admin/review-assigned-tasks-search?keyword=${encodeURIComponent(keyword)}&adminId=${encodeURIComponent(adminId)}&status=${encodeURIComponent(taskstatus)}`)
            .then(response => {
                const tableId = tabname === "empDone" ? "empDoneTasks" : "approvedTasks";

                renderPendingApprovedTasksontable(response.data, tableId, "reviewtaskEdit-btn");
                if (tableId == 'approvedTasks') {
                    renderPendingApprovedTasksontable(response.data, tableId, "reviewtaskEdit-btn", "hide-edit");
                }
            })
            .catch(err => {
                console.error("Search error:", err);
            });
    }


    $("#search_donetask_btn").on("click", function () {
        searchReviewNApprovedTask("empDone");
    });
    $("#search_donetask_input").on("keyup", function (e) {
        if (e.key === "Enter") searchReviewNApprovedTask("empDone");
    });

    $("#search_approvedtask_btn").on("click", function () {
        searchReviewNApprovedTask("approved");
    });
    $("#search_approvedtask_input").on("keyup", function (e) {
        if (e.key === "Enter") searchReviewNApprovedTask("approved");
    });


    // filter of employe list.html
    $("#emp-filter-options .dropdown-item").on("click", function () {
        const sortType = $(this).data("sort");
        getApiData(`http://localhost:8080/admin/filter-users?sort=${sortType}`)
            .then(data => {
                renderempListontable(data.data, "employee-list")
            })
            .catch(err => console.error("Error fetching users:", err));
    });


    function searchEmployee() {
        const keyword = $("#employee-search-bar input").val().trim();
        if (!keyword) return loadEmployeeList();
        const adminId = localStorage.getItem("userId");
        getApiData(`http://localhost:8080/admin/search-users?keyword=${encodeURIComponent(keyword)}`)
            .then(response => {
                renderempListontable(response.data, "employee-list");
            })
            .catch(err =>
                console.error("Search error:", err));
    }

    $("#employee_search_btn").on("click", searchEmployee);
    $("#employee_search_input").on("keyup", function (e) {
        if (e.key === "Enter") searchEmployee();
    });


    // logout
    $("#logout").on("click", function (e) {
        getApiData("http://localhost:8080/logout")
            .then(res => {
                console.log(res.message)
            })
            .catch(err => {
                console.log(err.message)
            });
        logout();

    })
});