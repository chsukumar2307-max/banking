# PowerShell script to create a new feature branch and specification directory for Speckit
param(
    [string]$Json,
    [int]$Number,
    [string]$ShortName,
    [string]$Description
)

# Set branch and spec directory names
$branchName = "$Number-$ShortName"
$specDir = "specs/$branchName"

# Create new branch
Write-Host "Creating new branch: $branchName"
git checkout -b $branchName

# Create spec directory
Write-Host "Creating spec directory: $specDir"
New-Item -ItemType Directory -Path $specDir -Force | Out-Null

# Create spec.md file
$specFile = "$specDir/spec.md"
Write-Host "Writing spec file: $specFile"
$specContent = @"
# Feature Spec: $branchName

## Description
$Description

## Created
$(Get-Date -Format 'yyyy-MM-dd')

## Actors
- System (backend)
- Account holder (initiator of transfer)
- Admin/user viewing transaction history

## Actions
- Add 'statement' field to transaction table/entity
- Populate field for transfer transactions
- Update transfer method
- Include field in DTOs
- Update migration scripts

## Data
- Transaction table/entity
- Statement field (string, format: 'fromAccount: ACCxxxx, toAccount: ACCyyyy')
- Source/destination account numbers

## Constraints
- Statement field only populated for transfer transactions
- Field must be included in DTOs and migration scripts

## Success Criteria
- All transfer transactions have a correctly formatted statement field
- Non-transfer transactions have a null or empty statement field
- Statement field is visible in transaction DTOs and API responses
- Migration script successfully adds the field without data loss

## Assumptions
- Statement field is nullable for non-transfer transactions
- Format is fixed as 'fromAccount: <source>, toAccount: <destination>'
- All transaction DTOs include the statement field
"@

Set-Content -Path $specFile -Value $specContent

Write-Host "Feature branch and spec directory created successfully."

