$BASE = "src\main\java\com\github\hoangducmanh\smart_task_management"
$RES  = "src\main\resources"

$dirs = @(
    # bootstrap
    "$BASE\bootstrap\config"
    "$BASE\bootstrap\properties"

    # domain
    "$BASE\domain\shared\model"
    "$BASE\domain\shared\valueobject"
    "$BASE\domain\shared\event"
    "$BASE\domain\shared\exception"

    "$BASE\domain\task\model"
    "$BASE\domain\task\policy"
    "$BASE\domain\task\service"
    "$BASE\domain\task\exception"

    "$BASE\domain\user\model"
    "$BASE\domain\user\exception"

    "$BASE\domain\comment\model"
    "$BASE\domain\comment\exception"

    # application
    "$BASE\application\shared\dto"
    "$BASE\application\shared\port\out"
    "$BASE\application\shared\exception"

    "$BASE\application\task\dto\command"
    "$BASE\application\task\dto\result"
    "$BASE\application\task\port\in"
    "$BASE\application\task\port\out"
    "$BASE\application\task\usecase"

    "$BASE\application\user\dto\command"
    "$BASE\application\user\dto\result"
    "$BASE\application\user\port\in"
    "$BASE\application\user\port\out"
    "$BASE\application\user\usecase"

    "$BASE\application\comment\dto\command"
    "$BASE\application\comment\dto\result"
    "$BASE\application\comment\port\in"
    "$BASE\application\comment\port\out"
    "$BASE\application\comment\usecase"

    # infrastructure
    "$BASE\infrastructure\persistence\config"

    "$BASE\infrastructure\persistence\task\entity"
    "$BASE\infrastructure\persistence\task\repository"
    "$BASE\infrastructure\persistence\task\mapper"
    "$BASE\infrastructure\persistence\task\adapter"

    "$BASE\infrastructure\persistence\user\entity"
    "$BASE\infrastructure\persistence\user\repository"
    "$BASE\infrastructure\persistence\user\mapper"
    "$BASE\infrastructure\persistence\user\adapter"

    "$BASE\infrastructure\persistence\comment\entity"
    "$BASE\infrastructure\persistence\comment\repository"
    "$BASE\infrastructure\persistence\comment\mapper"
    "$BASE\infrastructure\persistence\comment\adapter"

    "$BASE\infrastructure\cache\config"
    "$BASE\infrastructure\cache\task\adapter"

    "$BASE\infrastructure\scheduler\task"
    "$BASE\infrastructure\audit\aop"
    "$BASE\infrastructure\observability\logging"

    # web
    "$BASE\web\config"
    "$BASE\web\security\jwt"
    "$BASE\web\security\filter"
    "$BASE\web\security\handler"
    "$BASE\web\exception"

    "$BASE\web\task\controller"
    "$BASE\web\task\dto"
    "$BASE\web\task\mapper"
    "$BASE\web\task\validator"

    "$BASE\web\user\controller"
    "$BASE\web\user\dto"
    "$BASE\web\user\mapper"
    "$BASE\web\user\validator"

    "$BASE\web\comment\controller"
    "$BASE\web\comment\dto"
    "$BASE\web\comment\mapper"
    "$BASE\web\comment\validator"

    # resources
    "$RES\db\migration"
    "$RES\db\seed"
)

foreach ($dir in $dirs) {
    New-Item -ItemType Directory -Force -Path $dir | Out-Null
    # tạo .gitkeep để git track folder trống
    New-Item -ItemType File -Force -Path "$dir\.gitkeep" | Out-Null
}

Write-Host "Done! Folder structure created." -ForegroundColor Green