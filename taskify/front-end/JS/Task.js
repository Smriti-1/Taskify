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

    //     <a href="#" class="nav-item">
    //     <i class="fas fa-cog nav-icon"></i> Settings
    // </a>
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

        const params = new URLSearchParams(window.location.search);
        const taskId =
            params.get("editTaskId") ||
            params.get("editDoneTaskId") ||
            params.get("editOverduetasksId") ||
            params.get("editRedoTaskId") ||
            params.get("editAssignTask");

        if (taskId) {
            populatedTaskFormOnEdit(taskId);
        } else {
            if (role?.toUpperCase() === "USER") {
                populateAdminDropdown();
            } else if (role?.toUpperCase() === "ADMIN") {
                populateUserDropdown();
            }
        }
    }

    function applyRoleBasedUI(role) {
        if (role?.toUpperCase() === "ADMIN") {
            $("#userAssignmentField").show();
            $("#adminAssignmentField").hide();
            $('option[value="Redo"], option[value="Overdue"]').remove();
            if (page.includes("FormTask.html") && params.has('editAssignTask')) {
                ["Redo", "Overdue"].forEach(value => {
                    if ($(`#status option[value="${value}"]`).length === 0) {
                        $('#status').append(`<option value="${value}">${value}</option>`);
                    }
                });

            }
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
    //populating Admin dropdown
    function populateAdminDropdown(selectedAdminId = null) {
        getApiData("http://localhost:8080/tasks/admins-list")
            .then(adminslist => {
                const dropdown = $("#adminId");
                dropdown.empty();
                dropdown.append('<option value="">--Select Admin--</option>');

                adminslist.forEach(ad => {
                    dropdown.append(`<option value="${ad.adminId}" data-email="${ad.adminEmail}">${ad.fullname} </option>`);
                });

                if (selectedAdminId) {
                    $("#adminId").val(selectedAdminId);
                }
            })
            .catch(err => {
                console.error("Failed to load categories :", err);
            });
    }

    //populating user dropdown
    function populateUserDropdown(selecteduserId = null) {
        getApiData("http://localhost:8080/admin/users-list")
            .then(users => {
                const dropdown = $("#userAssign");
                dropdown.empty();
                dropdown.append('<option value="">--Select User--</option>');

                // console.log(users)
                users.data.forEach(user => {
                    dropdown.append(`<option value="${user.userId}" data-email="${user.email}">${user.firstname} ${user.lastname} - (${user.email})</option>`);
                });
                if (selecteduserId) {
                    $("#userAssign").val(selecteduserId);
                }
            })
            .catch(err => {
                console.error("Failed to load users list: ", err);
            });
    }

    // submit form of task on add task and edit task by user

    $("#task_form").on("submit", function (e) {
        const role = localStorage.getItem("role");
        const fromPage = params.get("from")
        e.preventDefault();

        const hasScript = /<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi;

        const editTaskId = $("input[name = 'editaskid' ]").val();
        const title = $("input[name='title']").val();
        const description = $("textarea[name='description']").val();
        const task_category = $("select[name='task_category']").val();
        let status = $("select[name='status']").val();
        const priority = $("select[name='priority']").val();
        const createdtime = $("input[name='createdtime']").val();
        const duedate = $("input[name='duedate']").val();

        const remark = $("textarea[name='remark']").val();
        const assignedAdminId = (role?.toUpperCase() === 'USER') ? $("select[name='adminId']").val() : null;
        const assignedUserId = (role?.toUpperCase() === 'ADMIN') ? $("select[name='userIdForAdmin']").val() : null;

        if (status == null && fromPage == "redo") {
            status = "Redo";
        } else if (status == null && fromPage == "overdue") {
            status = "Overdue";
        }
        // console.log(status)
        // console.log(assignedUserId);
        const fields = [
            { name: "Title", value: title },
            { name: "Description", value: description },
            { name: "Category", value: task_category },
            { name: "Status", value: status },
            { name: "Priority", value: priority },
            { name: "Created Time", value: createdtime },
            { name: "Due Date", value: duedate }
        ];

        if (role?.toUpperCase() === 'USER') {
            fields.push({ name: "Assigning Admin", value: assignedAdminId });
        }
        if (role?.toUpperCase() === 'ADMIN') {
            fields.push({ name: "Assigning User", value: assignedUserId });
        }

        if (duedate < createdtime) {
            e.preventDefault();
            showAlert("taskAlert", "Due date cannot be earlier then creation date");
            return;
        }

        if (createdtime > duedate) {
            e.preventDefault();
            showAlert("taskAlert", "Created date cannot be after the due date");
            return;
        }
        for (let field of fields) {
            if (field.value === undefined || field.value === null || field.value === "" || Number.isNaN(field.value)) {
                showAlert("taskAlert", `${field.name} is required.`);
                return;
            }
        }

        if (hasScript.test(title) || hasScript.test(description) || hasScript.test(remark)) {
            showAlert("taskAlert", "Invalid input: Script tag detected.");
            return;
        }


        const taskData = {
            title,
            description,
            status,
            priority,
            category: task_category,
            createdTime: createdtime,
            dueDate: duedate,
            assignedToUserId: assignedUserId,
            isApproved: false,
            assignedAdminId
        };

        if (role?.toLowerCase() === "admin") {
            taskData.adminRemark = remark;
        } else if (role?.toLowerCase() === "user") {
            taskData.userRemark = remark;
        }

        const apiUrl = editTaskId
            ? `http://localhost:8080/tasks/update/task=${editTaskId}`
            : "http://localhost:8080/tasks/create";

        apiRequest(apiUrl, taskData)
            .then(res => {
                showAlert("taskAlert", res.message, "success");

                if (role?.toLowerCase() === "user") {
                    if (fromPage === "review") {
                        window.location.href = './CompApp.html';
                    } else if (fromPage === "overdue") {
                        window.location.href = './OverdueTasks.html';
                    } else if (fromPage === "redo") {
                        window.location.href = './RedoTasks.html';
                    } else {
                        window.location.href = './AllTasks.html';
                    }
                } else if (role?.toLowerCase() === "admin") {
                    window.location.href = './AllAdminCreatedTask.html';
                }
            })
            .catch(err => {
                showAlert("taskAlert", err.message);
            });

    });

    // view user all task by user
    if (page.includes("AllTasks.html") && role == "user") {
        loadUserTask();
    }

    function loadUserTask() {
        const id = localStorage.getItem("userId");
        getApiData(`http://localhost:8080/tasks/getall/user${id}`)
            .then(tasks => {
                renderTasksontable(tasks.data, "userAllTasks", "newtaskEdit-btn");
            })
            .catch(err => {
                showAlert("taskAlert", err.message);
            });

    }

    // handle edit task by user at create
    $(document).on('click', ".newtaskEdit-btn", function () {
        const taskId = $(this).data('taskid');
        window.location.href = `./FormTask.html?editTaskId=${taskId}`;
    })

    const taskId = params.get('editTaskId') || params.get('editDoneTaskId') || params.get('editOverduetasksId') || params.get('editRedoTaskId') || params.get('editAssignTask');

    if (page.includes("FormTask.html") && taskId) {
        populatedTaskFormOnEdit(taskId);
    }


    function populatedTaskFormOnEdit(editTaskId) {
        getApiData(`http://localhost:8080/tasks/get/${editTaskId}`)
            .then(res => {
                let task = res.data;
                // console.log(task.user.userId)
                //    console.log(formatCustomDate(task.createdTime))
                $("input[name ='editaskid']").val(task.taskId);
                $("input[name='title']").val(task.title);
                $("textarea[name='description']").val(task.description);
                $("select[name='task_category']").val(task.category);
                $("select[name='status']").val(task.status);
                $("select[name='priority']").val(task.priority);
                $("input[name='createdtime']").val(formatCustomDate(task.createdTime, true));
                $("input[name='duedate']").val(formatCustomDate(task.dueDate, true));
                const role = localStorage.getItem("role");
                if (role?.toUpperCase() === 'USER') {
                    $("textarea[name='remark']").val(task.userRemark);
                    if (task.assignedAdminId == null) {
                        $("select[name='adminId']").val(task.admin.adminId);
                    } else {

                        populateAdminDropdown(task.assignedAdminId);
                        // $("select[name='adminId']").val(task.assignedAdminId);
                    }
                } else if (role?.toUpperCase() === 'ADMIN') {
                    $("textarea[name='remark']").val(task.adminRemark);
                    populateUserDropdown(task.assignedToUserId || task.user.userId);
                    // console.log(task.assignedToUserId)
                    //    $("select[name='userIdForAdmin']").val(task.assignedToUserId);

                }

                $("#submit_task_btn").text("Update Task");
                $("#task_form_heading").text("Edit Task");
            })
            .catch(err => {
                // console.error("Error loading task:", err);
                showAlert("taskAlert", err.message);
            });
    }

    // handel delete task user
    $(document).on("click", ".delete_task", function () {
        const taskid = $(this).attr('data-taskID');

        if (confirm("Are you sure you want to delete this task?")) {
            apiRequest(`http://localhost:8080/tasks/delete/${taskid}`, "", 'DELETE')
                .then(res => {
                    if (res.message != "") {
                        showAlert("taskviewalert", res.message, "success");
                    } else {
                        $("#taskviewalert").html("");
                    }
                    loadUserTask();
                })
                .catch(err => {
                    showAlert("taskviewalert", err.message);
                });
        }
    });
    // handle edit that are waiting for approval
    $(document).on("click", ".reviewtaskEdit-btn", function () {
        const taskId = $(this).data('taskid');
        window.location.href = `./FormTask.html?editDoneTaskId=${taskId}&from=review`;
    })

    // handle edit of overdue tasks
    $(document).on("click", ".overDuetaskEdit-btn", function () {
        const taskId = $(this).data('taskid');
        window.location.href = `./FormTask.html?editOverduetasksId=${taskId}&from=overdue`;
    })

    // handle edit of redo tasks
    $(document).on("click", ".redotaskEdit-btn", function () {
        const taskId = $(this).data('taskid');
        window.location.href = `./FormTask.html?editRedoTaskId=${taskId}&from=redo`;
    })

    // fecth and populated task that are waiting for approval
    if (page.includes("CompApp.html") && role == "user") {
        loadDoneORCompletedUserTask("waiting");
    }
    $(document).on('click', '#waiting-tab', function () {
        loadDoneORCompletedUserTask("waiting");
    });

    $(document).on('click', '#approved-tab', function () {
        loadDoneORCompletedUserTask("approved");
    });

    // fetching aproval and pending for approval tasks
    function loadDoneORCompletedUserTask(tabName) {
        const id = localStorage.getItem("userId");
        let endpoint = "";

        if (tabName === "waiting") {
            endpoint = `http://localhost:8080/tasks/waitingforapproval/user${id}`;
        } else if (tabName === "approved") {
            endpoint = `http://localhost:8080/tasks/approved/user${id}`;
        }

        getApiData(endpoint)
            .then(tasks => {
                const tableId = tabName === "waiting" ? "waitingTasks" : "approvedTasks";

                renderTasksontable(tasks.data, tableId, "reviewtaskEdit-btn");
                if (tableId == "approvedTasks") {
                    renderTasksontable(tasks.data, tableId, "reviewtaskEdit-btn", "hide-edit");

                }
            })
            .catch(err => {
                showAlert("taskAlert", err.message);
            });
    }
    // fetching over due tasks
    if (page.includes("OverdueTasks.html") && role == "user") {
        loadOverdueTask();
    }

    function loadOverdueTask() {
        const id = localStorage.getItem("userId");
        getApiData(`http://localhost:8080/tasks/getoverdue/user${id}`)
            .then(tasks => {
                renderTasksontable(tasks.data, "overdueTasks", "overDuetaskEdit-btn");
            })
            .catch(err => {
                showAlert("taskAlert", err.message);
            });

    }

    // fetching over due tasks
    if (page.includes("RedoTasks.html") && role == "user") {
        loadRedoTask();
    }

    function loadRedoTask() {
        const id = localStorage.getItem("userId");
        getApiData(`http://localhost:8080/tasks/getredo/user${id}`)
            .then(tasks => {
                renderTasksontable(tasks.data, "redoTasks", "redotaskEdit-btn");
            })
            .catch(err => {
                showAlert("taskAlert", err.message);
            });
    }



    function renderTasksontable(tasks, tableBodyId, edit_btn_id, disp_edit = null) {
        const $tableBody = $(`#${tableBodyId}`);
        $tableBody.empty();
        if (!tasks.length) return $tableBody.append(`<tr><td colspan="6" class="text-center">No Tasks Found.</td></tr>`);


        tasks.forEach((task, index) => {

            const dispEditBtn = disp_edit != null ? "" : `
            <td>
                <button class="action-btn ${edit_btn_id}" data-taskid="${task.taskId}" title="Edit Task">
                    <i class="fa fa-pencil-square"></i>
                </button>
            </td>
            <td>
                <button class="action-btn delete_task" data-taskID="${task.taskId}" title="Delete Task">
                    <i class="fa fa-trash"></i>
                </button>
            </td>`;

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
            <td>${formatCustomDate(task.createdTime)}</td>
            <td>
                <div class="application-status status-${task.status.toLowerCase().replace(/\s/g, '-')}">
                    ${task.status}
                </div>
            </td>
            <td>
                <span class="badge priority-badge-${task.priority.toLowerCase()}">${task.priority}</span>
            </td>
            <td>
                <table>
                    <tr>
                        <td>
                            <button class="action-btn view-task-btn" data-index="${index}" title="View Task">
                                <i class="fas fa-eye"></i>
                            </button>
                        </td>
                       ${dispEditBtn}
                        
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

            $('#modal-task-title').text(task.title);
            $('#modal-task-description').text(task.description || '—');
            $('#modal-task-status').text(task.status);
            $('#modal-task-priority').text(task.priority);
            $('#modal-task-category').text(task.category || '—');
            $('#modal-task-created').text(formatCustomDate(task.createdTime));
            $('#modal-task-due').text(formatCustomDate(task.dueDate));
            $('#modal-task-userRemark').text(task.userRemark || '—');
            $('#modal-task-adminRemark').text(task.adminRemark || '—');

            const modal = new bootstrap.Modal(document.getElementById('viewTaskModal'));
            modal.show();
        });
    }


    // search withing incomplete tasks
    // search_incompletetask
    function searchTasks(tasktype, searchboxID) {
        const keyword = $(`${searchboxID} input`).val().trim();
        if (!keyword) {
            if (tasktype == "incomplete") {
                return loadUserTask();
            } else if (tasktype == "done") {
                return loadDoneORCompletedUserTask("waiting");
            } else if (tasktype == "redo") {
                return loadRedoTask();
            } else if (tasktype == "overdue") {
                return loadOverdueTask();
            } else {
                window.location.reload();
            }
        }
        else
            getApiData(`http://localhost:8080/tasks/search?keyword=${encodeURIComponent(keyword)}&status=${encodeURIComponent(tasktype)} `)
                .then(response => {
                    const tasks = response.data;
                    if (tasktype == "incomplete") {
                        renderTasksontable(tasks, "userAllTasks", "newtaskEdit-btn");
                    }
                    else if (tasktype == "done") {
                        renderTasksontable(tasks, "waitingTasks", "reviewtaskEdit-btn");
                    } else if (tasktype == "redo") {
                        renderTasksontable(tasks, "redoTasks", "redotaskEdit-btn");
                    }
                    else if (tasktype == "overdue") {
                        renderTasksontable(tasks, "overdueTasks", "overduetaskEdit-btn");
                    }
                    else {
                        renderTasksontable(tasks, "userAllTasks", "newtaskEdit-btn");
                    }
                })
                .catch(err => console.error("Search error:", err));
    }

    // all task search
    $("#search_incompletetask_btn").on("click", function () {
        searchTasks("incomplete", "#incomp-search-bar");
    });
    $("#search_incompletetask_input").on("keyup", function (e) {
        if (e.key === "Enter") {
            searchTasks("incomplete", "#incomp-search-bar");
        }
    });

    // search redo task
    $("#search_redotask_btn").on("click", function () {
        searchTasks("redo", "#redo-search-bar");
    });
    $("#search_redotask_input").on("keyup", function (e) {
        if (e.key === "Enter") {
            searchTasks("redo", "#redo-search-bar");
        }
    });

    // search Overdue task
    $("#search_overduetask_btn").on("click", function () {
        searchTasks("overdue", "#overdue-search-bar");
    });
    $("#search_overduetask_input").on("keyup", function (e) {
        if (e.key === "Enter") {
            searchTasks("overdue", "#overdue-search-bar");
        }
    });

    // CompAPP search bar
    $("#search_donetask_btn").on("click", function () {
        searchTasks("done", "#waitApp-search-bar");
    });
    $("#search_donetask_input").on("keyup", function (e) {
        if (e.key === "Enter") {
            searchTasks("done", "#waitApp-search-bar");
        }
    });

    // filter based sort-options
    $("#wait-sort-options .dropdown-item").on("click", function () {
        let selectedSort = $(this).data("sort");
        fetchFilteredTasks("by-sort", `sort=${encodeURIComponent(selectedSort)}`, "done");
    });
    $("#incomp-sort-options .dropdown-item").on("click", function () {
        let selectedSort = $(this).data("sort");
        fetchFilteredTasks("by-sort", `sort=${encodeURIComponent(selectedSort)}`, "incomplete");
    });

    $("#redo-sort-options .dropdown-item").on("click", function () {
        let selectedSort = $(this).data("sort");
        fetchFilteredTasks("by-sort", `sort=${encodeURIComponent(selectedSort)}`, "redo");
    });

    $("#overdue-sort-options .dropdown-item").on("click", function () {
        let selectedSort = $(this).data("sort");
        fetchFilteredTasks("by-sort", `sort=${encodeURIComponent(selectedSort)}`, "overdue");
    });
    // filter-by-priority
    $("#wait-priority-options .dropdown-item").on("click", function () {
        let selectedPrioritiy = $(this).data("priority");
        fetchFilteredTasks("by-priority", `priority=${encodeURIComponent(selectedPrioritiy)}`, "done");
    });
    $("#incomp-priority-options .dropdown-item").on("click", function () {
        let selectedPrioritiy = $(this).data("priority");
        fetchFilteredTasks("by-priority", `priority=${encodeURIComponent(selectedPrioritiy)}`, "incomplete");
    });

    $("#redo-priority-options .dropdown-item").on("click", function () {
        let selectedPrioritiy = $(this).data("priority");
        fetchFilteredTasks("by-priority", `priority=${encodeURIComponent(selectedPrioritiy)}`, "redo");
    });
    $("#overdue-priority-options .dropdown-item").on("click", function () {
        let selectedPrioritiy = $(this).data("priority");
        fetchFilteredTasks("by-priority", `priority=${encodeURIComponent(selectedPrioritiy)}`, "overdue");
    });
    // filter-by-status
    $("#incomp-status-options .dropdown-item").on("click", function () {
        let selectedstatus = $(this).data("status");
        fetchFilteredTasks("by-status", `status=${encodeURIComponent(selectedstatus)}`, "incomplete");
    });

    function fetchFilteredTasks(method, selectedOption, tasktype) {
        getApiData(`http://localhost:8080/tasks/filter-${method}?&tasksStatus=${encodeURIComponent(tasktype)}&${selectedOption}`)
            .then(response => {
                console.log(response)
                if (response.message) {
                    showAlert("AlertMessage", response.message);
                } else {

                    $("#AlertMessage").text("")

                    let tasks = response.data;
                    if (tasktype == "incomplete") {
                        renderTasksontable(tasks, "userAllTasks", "newtaskEdit-btn");
                    }
                    else if (tasktype == "done") {
                        renderTasksontable(tasks, "waitingTasks", "reviewtaskEdit-btn");
                    } else if (tasktype == "redo") {
                        renderTasksontable(tasks, "redoTasks", "redotaskEdit-btn");
                    }
                }
            })
            .catch(error => {
                console.error(error);
            });
    }

    // logout
    $("#logout").on("click", function (e) {
        getApiData("http://localhost:8080/logout")
            .then(res => {
                console.log(res.message)
            })
            .catch(err => {
                console.log(res.message)
            });
        logout();

    })
});