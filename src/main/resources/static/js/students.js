function addStudent() {
    const container = document.getElementById('add-student-form');
    const nameInput = document.getElementById('studentName');
    const groupId = container.getAttribute('data-group-id');

    const data = {
        name: nameInput.value,
        groupId: groupId
    };

    fetch('/students/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
        .then(response => response.text())
        .then(html => {
        document.getElementById('result-container').innerHTML = html;
        nameInput.value = '';
    });
}

function deleteStudent(studentId) {
    if (!confirm("Удалить этого студента?")) return;

    const groupId = document.getElementById('add-student-form').getAttribute('data-group-id');
    const data = {
        studentId: studentId,
        groupId: groupId
    };

    fetch('/students/delete', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })

        .then(res => res.text())
        .then(html => {
        document.getElementById('result-container').innerHTML = html;
    })
        .catch(err => console.error("Ошибка при удалении:", err));
}