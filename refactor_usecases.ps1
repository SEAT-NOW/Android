$ErrorActionPreference = "Stop"

$basePath = "c:\Users\TehoonAhn\Documents\Depth_kotlin\4th-MainProject-SeatNow-Android\app\src\main\java\com\gmg\seatnow"
$usecasePath = "$basePath\domain\usecase"

$map = @{
    "FormatDateUseCase" = "common.logic";
    "FormatTimerUseCase" = "common.logic";
    "CalculateSpaceInfoUseCase" = "common.logic";
    "ValidateEmailUseCase" = "common.validation";
    "ValidatePasswordUseCase" = "common.validation";
    "CheckTestAccountUseCase" = "common.validation";
    
    "ReissueTokenUseCase" = "common.auth";
    "RequestEmailAuthCodeUseCase" = "common.auth";
    "RequestPhoneAuthCodeUseCase" = "common.auth";
    "VerifyEmailAuthCodeUseCase" = "common.auth";
    "VerifyPhoneAuthCodeUseCase" = "common.auth";

    "ChangeOwnerPasswordUseCase" = "owner.auth";
    "GetOwnerAccountUseCase" = "owner.auth";
    "OwnerLoginUseCase" = "owner.auth";
    "OwnerLogoutUseCase" = "owner.auth";
    "OwnerWithdrawUseCase" = "owner.auth";
    "SignUpOwnerUseCase" = "owner.auth";
    "VerifyBusinessNumberUseCase" = "owner.auth";
    "VerifyOwnerPasswordUseCase" = "owner.auth";

    "GetStoreProfileUseCase" = "owner.store";
    "UpdateStorePhoneUseCase" = "owner.store";
    "DeleteMenuUseCase" = "owner.store";
    "FormatMenuCategoryUseCase" = "owner.store";
    "GetStoreImagesUseCase" = "owner.store";
    "GetStoreOperationInfoUseCase" = "owner.store";
    "LimitStorePhotosUseCase" = "owner.store";
    "UploadMenuImageUseCase" = "owner.store";
    "UploadStoreImagesUseCase" = "owner.store";
    "SaveStoreDetailUseCase" = "owner.store";
    "UpdateStoreOperationUseCase" = "owner.store";
    "UpdateMenuCategoriesUseCase" = "owner.store";
    "UpdateMenuOrdersUseCase" = "owner.store";
    "UpdateStoreImagesUseCase" = "owner.store";
    "UpdateStoreOperationInfoUseCase" = "owner.store";
    "CheckScheduleCollisionUseCase" = "owner.store";
    "UpdateMenuUseCase" = "owner.store";

    "CalculateSeatCountUseCase" = "owner.seat";
    "GetSeatConfigurationUseCase" = "owner.seat";
    "GetSeatStatusUseCase" = "owner.seat";
    "CalculateSeatDisplayUseCase" = "owner.seat";
    "UpdateStoreLayoutUseCase" = "owner.seat";
    "UpdateTableStatusUseCase" = "owner.seat";
    "UpdateSeatUsageUseCase" = "owner.seat";
    "UpdateTableCountUseCase" = "owner.seat";

    "AutoLoginUseCase" = "user.auth";
    "CheckGuestTermsUseCase" = "user.auth";
    "CheckIsGuestUseCase" = "user.auth";
    "CheckKakaoTermsUseCase" = "user.auth";
    "GetSplashDestinationUseCase" = "user.auth";
    "GetUserNicknameUseCase" = "user.auth";
    "LoginWithKakaoUseCase" = "user.auth";
    "SaveGuestTermsUseCase" = "user.auth";
    "SaveKakaoTermsUseCase" = "user.auth";
    "SaveKakaoUserInfoUseCase" = "user.auth";
    "WithdrawUserUseCase" = "user.auth";
    "CheckDeveloperModeUseCase" = "user.auth";
    "SetDeveloperModeUseCase" = "user.auth";

    "GetKeepStoresUseCase" = "user.mypage";
    "ToggleKeepStoreUseCase" = "user.mypage";

    "GetNearbyUniversityUseCase" = "user.home";
    "SearchStoresUseCase" = "user.home";
    "ExtractNeighborhoodUseCase" = "user.home";
    "ValidateHeadCountUseCase" = "user.home";
    "AdjustHeadCountUseCase" = "user.home";
    "GetStoresByHeadCountUseCase" = "user.home";
    "GetStoresUseCase" = "user.home";

    "GetStoreDetailUseCase" = "user.detail";
    "GetStoreMenusUseCase" = "user.detail";
    "FormatStoreDetailUseCase" = "user.detail";
}

$files = Get-ChildItem -Path $usecasePath -Recurse -Filter "*.kt" | Where-Object { !($_.PSIsContainer) }

$replacements = @()

foreach ($f in $files) {
    $name = $f.BaseName
    $oldSub = $f.Directory.Name
    
    if ($map.ContainsKey($name)) {
        $newSub = $map[$name]
    } else {
        $newSub = $oldSub # keep it if not mapped
    }
    
    $oldImport = "com.gmg.seatnow.domain.usecase.$oldSub.$name"
    $newImport = "com.gmg.seatnow.domain.usecase.$newSub.$name"
    
    $newDir = Join-Path $usecasePath $newSub.Replace(".", "\")
    if (!(Test-Path $newDir)) {
        New-Item -ItemType Directory -Force -Path $newDir | Out-Null
    }
    
    $newPath = Join-Path $newDir $f.Name
    
    # Check if we need to do anything
    if ($f.FullName -eq $newPath) {
        continue
    }
    
    # Move file
    Move-Item -Path $f.FullName -Destination $newPath
    
    # Add to replacements
    $replacements += [PSCustomObject]@{ Old = $oldImport; New = $newImport }
    
    # Update package declaration in the file itself (regex exact match)
    $content = Get-Content $newPath -Raw
    $oldPkg = "package com.gmg.seatnow.domain.usecase.$oldSub"
    $newPkg = "package com.gmg.seatnow.domain.usecase.$newSub"
    $content = $content.Replace($oldPkg, $newPkg)
    [IO.File]::WriteAllText($newPath, $content)
}

# 2. Update imports globally
$allFiles = Get-ChildItem -Path $basePath -Recurse -Filter "*.kt" | Where-Object { !($_.PSIsContainer) }

foreach ($f in $allFiles) {
    if ($f.FullName -like "*\build\*") { continue }
    $content = Get-Content $f.FullName -Raw
    $modified = $false
    
    foreach ($rep in $replacements) {
        if ($content.Contains($rep.Old)) {
            $content = $content.Replace($rep.Old, $rep.New)
            $modified = $true
        }
    }
    
    if ($modified) {
        [IO.File]::WriteAllText($f.FullName, $content)
    }
}

Write-Output "Refactoring completed successfully!"
