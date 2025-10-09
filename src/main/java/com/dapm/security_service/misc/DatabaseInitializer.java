package com.dapm.security_service.misc;

import com.dapm.security_service.models.*;
import com.dapm.security_service.models.enums.OrgPermAction;
import com.dapm.security_service.models.enums.ProjectPermAction;
import com.dapm.security_service.models.enums.SubscriptionTier;
import com.dapm.security_service.models.enums.Tier;
import com.dapm.security_service.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    // Repositories
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private OrgRoleRepository orgRoleRepository;
    @Autowired private PipelineRepositoryy pipelineRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private OrgPermissionRepository orgPermissionRepository;
    @Autowired private ProjectsRolesRepository projectsRolesRepository;
    @Autowired private ProjPermissionRepository projectPermActionRepository;
    @Autowired private ProjectRolePermissionRepository projectRolePermissionRepository;
    @Autowired private UserRoleAssignmentRepository userRoleAssignmentRepository;

    @Autowired private TiersRepository tierRepository;
    @Autowired private VoucherRepository voucherRepository;


    @Autowired
    private ProcessingElementRepository processingElementRepository;


    // BCrypt encoder
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Default organization name from properties.
    @Value("${dapm.defaultOrgName}")
    private String orgName;

    // --- Static UUID Definitions (unique) ---
    // Organizations
    private static final UUID ORG_A_ID = UUID.fromString("3430e05b-3b59-48c2-ae8a-22a9a9232f18");


    // Permissions
    private static final UUID PERM_APPROVE_ACCESS_ID         = UUID.fromString("aaaaaaaa-1111-1111-1111-aaaaaaaaaaaa");
    private static final UUID PERM_SET_LIMITS_ID             = UUID.fromString("bbbbbbbb-2222-2222-2222-bbbbbbbbbbbb");
    private static final UUID PERM_READ_PIPELINE_ID          = UUID.fromString("cccccccc-3333-3333-3333-cccccccccccc");
    private static final UUID PERM_EXECUTE_PIPELINE_ID       = UUID.fromString("dddddddd-4444-4444-4444-dddddddddddd");
    private static final UUID PERM_REQUEST_ACCESS_ID         = UUID.fromString("eeeeeeee-5555-5555-5555-eeeeeeeeeeee");
    private static final UUID PERM_CONFIGURE_CROSS_ORG_TRUST_ID = UUID.fromString("ffffffff-6666-6666-6666-ffffffffffff");
    private static final UUID PERM_EXCHANGE_PUBLIC_KEYS_ID     = UUID.fromString("11111111-7777-7777-7777-111111117777");
    private static final UUID PERM_ROLE_MANAGEMENT_ID         = UUID.fromString("22222222-8888-8888-8888-222222228888");
    private static final UUID PERM_CREATE_PIPELINE_ID         = UUID.fromString("33333333-9999-9999-9999-333333339999");
    private static final UUID PERM_UPLOAD_RESOURCE_ID         = UUID.fromString("44444444-aaaa-aaaa-aaaa-44444444aaaa");
    private static final UUID PERM_DELETE_RESOURCE_ID         = UUID.fromString("55555555-bbbb-bbbb-bbbb-55555555bbbb");
    private static final UUID PERM_READ_RESOURCE_ID           = UUID.fromString("66666666-cccc-cccc-cccc-66666666cccc");
    private static final UUID PERM_EDIT_RESOURCE_ID           = UUID.fromString("77777777-dddd-dddd-dddd-77777777dddd");
    private static final UUID PERM_DOWNLOAD_RESOURCE_ID       = UUID.fromString("88888888-eeee-eeee-eeee-88888888eeee");
    private static final UUID PERM_ACCESS_RESOURCE_ID         = UUID.fromString("aaaaaaa0-0000-0000-0000-aaaaaaaa0001");
    private static final UUID PERM_MODIFY_RESOURCE_ID         = UUID.fromString("bbbbbbbb-0000-0000-0000-bbbbbbbb0001");

    // Roles for OrgA
    private static final UUID ROLE_ADMIN_ID        = UUID.fromString("cccccccc-1111-1111-1111-cccccccc1111");
    private static final UUID ROLE_DEPHEAD_ID      = UUID.fromString("dddddddd-2222-2222-2222-dddddddd2222");
    private static final UUID ROLE_RESEARCHER_ID   = UUID.fromString("eeeeeeee-3333-3333-3333-eeeeeeee3333");
    private static final UUID ROLE_PIPELINE_ID     = UUID.fromString("f17a2042-f9c8-4a46-83fc-5c83e1cb7aee"); // Given



    // Users for OrgA
    private static final UUID USER_ANNA_ID     = UUID.fromString("11111111-1111-1111-1111-111111111112");
    private static final UUID USER_ANTHONI_ID  = UUID.fromString("11111111-1111-1111-1111-111111111113");
    private static final UUID USER_ALICE_ID    = UUID.fromString("11111111-1111-1111-1111-111111111114");
    private static final UUID USER_ASHLEY_ID   = UUID.fromString("11111111-1111-1111-1111-111111111115");








    // Hey there I am new
    // Create a project uuid
    private static final UUID PROJECT1_ID = UUID.fromString("99999999-0000-0000-0000-999999999999");
    // Hey there I am new
    // Create a OrgRole
    private static final UUID ADMIN_ID = UUID.fromString("99999999-0000-0000-0000-299999999999");
    private static final UUID MEMBER_ID = UUID.fromString("99999999-0000-0000-0000-399999999999");

    // Hey there I am new
    // Create a Role for Project level
    private static final UUID RESEARCHER_ID = UUID.fromString("99999999-0001-0000-0000-299999999999");
    private static final UUID LEADER_ID = UUID.fromString("99999999-0031-0000-0000-299999999999");




    @Transactional
    @Override
    public void run(String... args) throws Exception {
        System.out.println("Default Org Name: " + orgName);

        Organization org;


//TODO: orgB IS  hardcoded , chnage later to be dynamic

        org = organizationRepository.findByName(orgName)
                .orElseGet(() -> organizationRepository.save(
                        Organization.builder()
                                .id(ORG_A_ID)
                                .name(orgName)
                                .build()
                ));

        // Hey there I am new
        // Create Org Roles for OrgA.
        OrgRole AdminOrgRole = createOrgRoleIfNotExistStatic("ADMIN", org, ADMIN_ID);
        OrgRole defaultOrgRole = createOrgRoleIfNotExistStatic("MEMBER", org, MEMBER_ID);

        // Hey there I am new
        // let's create the org permissions
        OrgPermission orgPermission1 = createOrgPermissionIfNotExistStatic(OrgPermAction.CREATE_USER, AdminOrgRole, UUID.randomUUID());
        OrgPermission orgPermission2 = createOrgPermissionIfNotExistStatic(OrgPermAction.DELETE_USER, AdminOrgRole, UUID.randomUUID());
        OrgPermission orgPermission3 = createOrgPermissionIfNotExistStatic(OrgPermAction.CREATE_PROJECT, AdminOrgRole, UUID.randomUUID());
        OrgPermission orgPermission8 = createOrgPermissionIfNotExistStatic(OrgPermAction.ASSIGN_PROJECT_ROLES, AdminOrgRole, UUID.randomUUID());
        OrgPermission orgPermission4 = createOrgPermissionIfNotExistStatic(OrgPermAction.READ_PROJECT, AdminOrgRole, UUID.randomUUID());
        OrgPermission orgPermission5 = createOrgPermissionIfNotExistStatic(OrgPermAction.UPDATE_PROJECT, AdminOrgRole, UUID.randomUUID());
        OrgPermission orgPermission6 = createOrgPermissionIfNotExistStatic(OrgPermAction.ASSIGN_ROLE, AdminOrgRole, UUID.randomUUID());
        OrgPermission orgPermission7 = createOrgPermissionIfNotExistStatic(OrgPermAction.DELETE_PROJECT, AdminOrgRole, UUID.randomUUID());
        OrgPermission orgPermission9 = createOrgPermissionIfNotExistStatic(OrgPermAction.CREATE_ROLE, AdminOrgRole, UUID.randomUUID());
        OrgPermission orgPermission10 = createOrgPermissionIfNotExistStatic(OrgPermAction.ASSIGN_USER_ROLE, AdminOrgRole, UUID.randomUUID());
        OrgPermission orgPermission12 = createOrgPermissionIfNotExistStatic(OrgPermAction.DELETE_ROLE, AdminOrgRole, UUID.randomUUID());
        OrgPermission orgPermission13 = createOrgPermissionIfNotExistStatic(OrgPermAction.APPROVE_REQUEST_PE, AdminOrgRole, UUID.randomUUID());

        // 6. Create Users for OrgA.
        createUserIfNotExistStatic("anna", "anna@example.com", "dapm", AdminOrgRole,org, USER_ANNA_ID);
        createUserIfNotExistStatic("charlie","charlie@gmail.com", "dapm", AdminOrgRole, org, UUID.fromString("11111111-1111-1111-1111-111111111116"));
        createUserIfNotExistStatic("anthoni", "anthoni@example.com", "dapm",AdminOrgRole, org, USER_ANTHONI_ID);
        createUserIfNotExistStatic("alice", "alice@example.com", "dapm",defaultOrgRole, org,  USER_ALICE_ID);
        createUserIfNotExistStatic("ashley", "ashley@example.com", "dapm",defaultOrgRole, org,  USER_ASHLEY_ID);



        // Hey there I am new
        // Create roles on project level
        ProjectRole projectRole1 = createProjectRoleIfNotExistStatic("researcher",RESEARCHER_ID);
        ProjectRole projectRole2 = createProjectRoleIfNotExistStatic("leader",LEADER_ID);

        //create a set of project roles of projectRole1 and projectRole2
        Set<ProjectRole> projectRolesSet = new HashSet<>();
        projectRolesSet.add(projectRole1);
        projectRolesSet.add(projectRole2);
        // Hey there I am new
        Project p=createProjectIfNotExistStatic("HospitalFlow",org,projectRolesSet,PROJECT1_ID);



        // Hey there I am new
        // create a project permission
        ProjectPermission projectPermission1=createProjectPermissionIfNotExistStatic(ProjectPermAction.READ_PE, UUID.randomUUID());
        ProjectPermission projectPermission2=createProjectPermissionIfNotExistStatic(ProjectPermAction.ACCESS_REQUEST_PE,UUID.randomUUID());
        ProjectPermission projectPermission3=createProjectPermissionIfNotExistStatic(ProjectPermAction.BUILD_PIPELINE, UUID.randomUUID());
        ProjectPermission projectPermission4=createProjectPermissionIfNotExistStatic(ProjectPermAction.EXECUTE_PIPELINE,UUID.randomUUID());
        ProjectPermission projectPermission5=createProjectPermissionIfNotExistStatic(ProjectPermAction.CONFIGURE_PIPELINE,UUID.randomUUID());
        ProjectPermission projectPermission6=createProjectPermissionIfNotExistStatic(ProjectPermAction.TERMINATE_PIPELINE,UUID.randomUUID());
        //ProjectPermission projectPermission7=createProjectPermissionIfNotExistStatic(ProjectPermAction.READ_PES, UUID.randomUUID());
        ProjectPermission projectPermission7=createProjectPermissionIfNotExistStatic(ProjectPermAction.UPDATE_PROJECT,UUID.randomUUID());
        ProjectPermission projectPermission8=createProjectPermissionIfNotExistStatic(ProjectPermAction.ASSIGN_USER_PROJECT_ROLE,UUID.randomUUID());
        ProjectPermission projectPermission9=createProjectPermissionIfNotExistStatic(ProjectPermAction.CREATE_PIPELINE,UUID.randomUUID());
        ProjectPermission projectPermission10=createProjectPermissionIfNotExistStatic(ProjectPermAction.VALIDATE_PIPELINE,UUID.randomUUID());
        ProjectPermission projectPermission11=createProjectPermissionIfNotExistStatic(ProjectPermAction.CREATE_PIPELINE,UUID.randomUUID());
        //ProjectPermission projectPermission13=createProjectPermissionIfNotExistStatic(ProjectPermAction.CREATE_PIPELINE,UUID.randomUUID());



        //ProjectPermission projectPermission11=createProjectPermissionIfNotExistStatic(ProjectPermAction.APPROVE_REQUEST_PE,UUID.randomUUID());






        ProjectRolePermission projectRolePermission=createProjectRolePermissionIfNotExistStatic(p,projectPermission1,projectRole2);
        ProjectRolePermission projectRolePermission2=createProjectRolePermissionIfNotExistStatic(p,projectPermission2,projectRole2);
        ProjectRolePermission projectRolePermission3=createProjectRolePermissionIfNotExistStatic(p,projectPermission3,projectRole2);
        ProjectRolePermission projectRolePermission4=createProjectRolePermissionIfNotExistStatic(p,projectPermission4,projectRole2);
        ProjectRolePermission projectRolePermission5=createProjectRolePermissionIfNotExistStatic(p,projectPermission5,projectRole2);
        ProjectRolePermission projectRolePermission6=createProjectRolePermissionIfNotExistStatic(p,projectPermission6,projectRole2);
        ProjectRolePermission projectRolePermission7=createProjectRolePermissionIfNotExistStatic(p,projectPermission7,projectRole2);
        ProjectRolePermission projectRolePermission8=createProjectRolePermissionIfNotExistStatic(p,projectPermission8,projectRole2);
        ProjectRolePermission projectRolePermission9=createProjectRolePermissionIfNotExistStatic(p,projectPermission9,projectRole2);
        ProjectRolePermission projectRolePermission10=createProjectRolePermissionIfNotExistStatic(p,projectPermission10,projectRole2);
        ProjectRolePermission projectRolePermission11=createProjectRolePermissionIfNotExistStatic(p,projectPermission11,projectRole2);

        ProjectRolePermission projectRolePermission12=createProjectRolePermissionIfNotExistStatic(p,projectPermission1,projectRole1);
//        ProjectRolePermission projectRolePermission13=createProjectRolePermissionIfNotExistStatic(p,projectPermission2,projectRole1);
        ProjectRolePermission projectRolePermission113=createProjectRolePermissionIfNotExistStatic(p,projectPermission3,projectRole1);
        ProjectRolePermission projectRolePermission14=createProjectRolePermissionIfNotExistStatic(p,projectPermission4,projectRole1);
//        ProjectRolePermission projectRolePermission15=createProjectRolePermissionIfNotExistStatic(p,projectPermission5,projectRole1);
        ProjectRolePermission projectRolePermission16=createProjectRolePermissionIfNotExistStatic(p,projectPermission6,projectRole1);
        ProjectRolePermission projectRolePermission17=createProjectRolePermissionIfNotExistStatic(p,projectPermission7,projectRole1);
//        ProjectRolePermission projectRolePermission18=createProjectRolePermissionIfNotExistStatic(p,projectPermission8,projectRole1);
        ProjectRolePermission projectRolePermission19=createProjectRolePermissionIfNotExistStatic(p,projectPermission9,projectRole1);
        ProjectRolePermission projectRolePermission110=createProjectRolePermissionIfNotExistStatic(p,projectPermission10,projectRole1);
        ProjectRolePermission projectRolePermission111=createProjectRolePermissionIfNotExistStatic(p,projectPermission11,projectRole1);


        User user = userRepository.findByUsername("anna")
                .orElseThrow(() -> new RuntimeException("User 'anna' not found"));

        UserRoleAssignment userRoleAssignment = createUserRoleAssignmentIfNotExist(user, p, projectRole2);



        Voucher voucher1=createVoucherIfNotExistStatic("BASIC-2025-ORG",false, SubscriptionTier.BASIC);
        Voucher voucher2=createVoucherIfNotExistStatic("PREMIUM-2025-ORG",false, SubscriptionTier.PREMIUM);
        Voucher voucher3=createVoucherIfNotExistStatic("BASIC2-2025-ORG",false, SubscriptionTier.BASIC);
        Voucher voucher4=createVoucherIfNotExistStatic("PREMIUM2-2025-ORG",false, SubscriptionTier.PREMIUM);


        Tiers tier1=createTierIfNotExistStatic(Tier.FREE,10);
        Tiers tier2=createTierIfNotExistStatic(Tier.BASIC,100);
        Tiers tier3=createTierIfNotExistStatic(Tier.PREMIUM,1000);


        System.out.println("Database initialization complete.");
    }

    // --- Helper Methods ---


    // create a createVoucherIfNotExistStatic method
    private Voucher createVoucherIfNotExistStatic(String code, boolean redeemed, SubscriptionTier subscriptionTier) {
        Voucher voucher = voucherRepository.findByCode(code).orElse(null);
        if (voucher == null) {
            voucher = Voucher.builder()
                    .code(code)
                    .tier(subscriptionTier )
                    .redeemed(redeemed)
                    .build();
            voucher = voucherRepository.save(voucher);
        }
        return voucher;
    }


    private void createUserIfNotExistStatic(String username, String email, String rawPassword, OrgRole orgRole,
                                            Organization organization, UUID id) {
        userRepository.findByUsername(username).orElseGet(() -> {
            String passwordHash = passwordEncoder.encode(rawPassword);
            User user = User.builder()
                    .id(id)
                    .username(username)
                    .email(email)
                    .passwordHash(passwordHash)
                    .organization(organization)
                    .orgRole(orgRole)
                    .build();
            return userRepository.save(user);
        });
    }

    private OrgPermission createOrgPermissionIfNotExistStatic(OrgPermAction action, OrgRole orgRole, UUID id) {
        OrgPermission orgPermission = orgPermissionRepository.findByAction(action);
        if (orgPermission == null) {
            orgPermission = OrgPermission.builder()
                    .id(id)
                    .action(action)
                    .orgRole(orgRole)
                    .build();
            orgPermission = orgPermissionRepository.save(orgPermission);
        }
        return orgPermission;
    }

    // create a creatOrgRoleIfNotExistStatic method
    private OrgRole createOrgRoleIfNotExistStatic(String name, Organization organization, UUID id) {
        OrgRole orgRole = orgRoleRepository.findByName(name);
        if (orgRole == null) {
            orgRole = OrgRole.builder()
                    .id(id)
                    .name(name)
                    .organization(organization)
                    .build();
            orgRole = orgRoleRepository.save(orgRole);
        }
        return orgRole;
    }

    private Project createProjectIfNotExistStatic(String name, Organization organization, Set<ProjectRole> projectRolesSet, UUID id) {
        Optional<Project> optionalProject = projectRepository.findByName(name);
        Project p;
        if (optionalProject.isPresent()) {
            p = optionalProject.get();
            if (p.getProjectRoles() == null || p.getProjectRoles().isEmpty()) {
                p.setProjectRoles(projectRolesSet);   // <-- ensure roles are assigned
                p = projectRepository.save(p);
            }
        } else {
            p = Project.builder()
                    .id(id)
                    .name(name)
                    .organization(organization)
                    .projectRoles(projectRolesSet)
                    .build();
            p = projectRepository.save(p);
        }
        return p;
    }


    private ProjectRole createProjectRoleIfNotExistStatic(String name, UUID id) {
        ProjectRole proRole = projectsRolesRepository.findByName(name);
        if (proRole == null) {
            proRole = ProjectRole.builder()
                    .id(id)
                    .name(name)
                    .build();
            proRole = projectsRolesRepository.save(proRole);
        }
        return proRole;
    }

    private ProjectPermission createProjectPermissionIfNotExistStatic(ProjectPermAction action, UUID id) {
        ProjectPermission projectPermission = projectPermActionRepository.findByAction(action);
        if (projectPermission == null) {
            projectPermission = ProjectPermission.builder()
                    .id(id)
                    .action(action)

                    .build();
            projectPermission = projectPermActionRepository.save(projectPermission);
        }
        return projectPermission;
    }

    private ProjectRolePermission createProjectRolePermissionIfNotExistStatic(Project project, ProjectPermission projectPermission, ProjectRole projectRole) {
        return projectRolePermissionRepository.findByProjectAndPermissionAndRole(project, projectPermission, projectRole)
                .orElseGet(() -> {
                    ProjectRolePermission newPermission = ProjectRolePermission.builder()
                            .id(UUID.randomUUID())
                            .permission(projectPermission)
                            .project(project)
                            .role(projectRole)
                            .build();
                    System.out.println(newPermission + " yooooooooo");
                    return projectRolePermissionRepository.save(newPermission);
                });
    }

    private UserRoleAssignment createUserRoleAssignmentIfNotExist(User user, Project project, ProjectRole role) {
        return userRoleAssignmentRepository.findByUserAndProject(user, project)
                .orElseGet(() -> {
                    UserRoleAssignment newAssignment = UserRoleAssignment.builder()
                            .user(user)
                            .project(project)
                            .role(role)
                            .build();
                    return userRoleAssignmentRepository.save(newAssignment);
                });
    }

    // create a createTierIfNotExistStatic method
    private Tiers createTierIfNotExistStatic(Tier name, Integer maxHours) {
        Tiers tier = tierRepository.findByName(name).orElse(null);
        if (tier == null) {
            tier = Tiers.builder()
                    .name(name)
                    .maxHours(maxHours)
                    .build();
            tier = tierRepository.save(tier);
        }
        return tier;
    }




}
