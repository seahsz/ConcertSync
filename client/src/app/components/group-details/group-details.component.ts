import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { GroupService } from '../../services/group.service';
import { AuthService } from '../../services/auth.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { catchError, interval, Subscription, switchMap } from 'rxjs';
import { GroupMember, GroupMessage, GroupWithMembers } from '../../models/group.models';
import { MessageRequest } from '../../models/message-request';
import { Title } from '@angular/platform-browser';

@Component({
  selector: 'app-group-details',
  standalone: false,
  templateUrl: './group-details.component.html',
  styleUrl: './group-details.component.css'
})
export class GroupDetailsComponent implements OnInit, OnDestroy {

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private groupSvc = inject(GroupService);
  private authSvc = inject(AuthService);
  private snackBar = inject(MatSnackBar);
  private fb = inject(FormBuilder);
  private title = inject(Title);

  // Component state
  protected group!: GroupWithMembers;
  protected members: GroupMember[] = [];
  protected messages: GroupMessage[] = [];
  protected pendingRequests: any[] = [];
  protected currentUserRole: 'creator' | 'member' | 'pending' | 'not-member' = 'not-member';
  protected isLoading = true;
  protected messageForm!: FormGroup;
  protected pendingRequestsLoading = false;
  protected messagesLoading = false;
  protected showChatSection = true;
  protected showMembersSection = true;
  protected showRequestsSection = false;

  // User state
  protected currentUserId: number = 0;
  protected isCreator = false;

  // Track subscriptions for cleanup
  private groupSub?: Subscription;
  private messageSub?: Subscription;
  private pendingRequestsSub?: Subscription;
  private messagePollingSubscription?: Subscription;

  ngOnInit(): void {
    // Get current user ID
    const userId = this.authSvc.getCurrentUserId();
    if (userId === null) {
      // Handle invalid user state
      this.snackBar.open("Authentication error", "Close", { duration: 3000 });
      this.router.navigate(['/login']);
      return;
    }

    this.currentUserId = userId;

    this.initMessageForm();
    this.loadGroupDetails(); // Set the title inside here coz of async
    this.setupMessagePolling();
  }

  ngOnDestroy(): void {
    // Clean up subscriptions
    if (this.groupSub) this.groupSub.unsubscribe();
    if (this.messageSub) this.messageSub.unsubscribe();
    if (this.pendingRequestsSub) this.pendingRequestsSub.unsubscribe();
    if (this.messagePollingSubscription) this.messagePollingSubscription.unsubscribe();
  }

  private initMessageForm(): void {
    this.messageForm = this.fb.group({
      message: [''] // Handling validation manually in submit and isMessageVali() function for [disabled]
      // because i do not want the red border when input field is invalid
    });
  }

  // Method to check if message is invalid (empty or too long)
  protected isMessageInvalid(): boolean {
    const message = this.messageForm.get('message')?.value || '';
    const trimmedMessage = message.trim();

    // Check for empty message or message exceeding max length (500)
    return trimmedMessage === '' || trimmedMessage.length > 500;
  }

  private loadGroupDetails(): void {
    this.isLoading = true;
    const groupId = this.route.snapshot.paramMap.get('id');

    if (!groupId) {
      this.snackBar.open('Group ID not provided', 'Close', { duration: 3000 });
      this.router.navigate(['/groups']);
      return;
    }

    this.groupSub = this.groupSvc.getGroupDetails(+groupId).subscribe({
      next: (response: GroupWithMembers) => {
        this.group = response;
        this.members = response.members;

        // Set title
        this.title.setTitle(`${this.group.name} | Concert Groups | ConcertSync`);

        // Determine current user's role
        if (this.group.creatorId === this.currentUserId) {
          this.currentUserRole = 'creator';
          this.isCreator = true;
          this.loadPendingRequests();
        } else if (this.members.some(member => member.id === this.currentUserId)) {
          this.currentUserRole = 'member';
        } else {
          // Check if user has a pending request
          this.groupSvc.checkMembershipStatus(+groupId).subscribe((status: string) => {
            if (status === 'pending') {
              this.currentUserRole = 'pending';
            } else {
              this.currentUserRole = 'not-member';
            }
          });
        }

        this.isLoading = false;
        this.loadGroupMessages();
      },
      error: (error) => {
        console.error('Error loading group details:', error);
        this.snackBar.open('Failed to load group details', 'Close', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  private loadPendingRequests(): void {
    if (!this.group || !this.isCreator) return;

    this.pendingRequestsLoading = true;
    this.pendingRequestsSub = this.groupSvc.getPendingRequests(this.group.id).subscribe({
      next: (requests) => {
        this.pendingRequests = requests;
        this.pendingRequestsLoading = false;
      },
      error: (error) => {
        console.error('Error loading pending requests:', error);
        this.pendingRequestsLoading = false;
      }
    });
  }

  private loadGroupMessages(): void {
    if (!this.group) return;

    this.messagesLoading = true;
    this.messageSub = this.groupSvc.getGroupMessages(this.group.id).subscribe({
      next: (messages: GroupMessage[]) => {
        this.messages = messages;
        this.messagesLoading = false;
        this.scrollToBottom();
      },
      error: (error) => {
        console.error('Error loading messages:', error);
        this.messagesLoading = false;
      }
    });
  }

  private setupMessagePolling(): void {
    // Poll for new messages every 10 seconds
    this.messagePollingSubscription = interval(10000).pipe(
      switchMap(() => {
        if (!this.group) {
          return [];
        }
        return this.groupSvc.getGroupMessages(this.group.id).pipe(
          catchError(error => {
            console.error('Error polling messages:', error);
            return [];
          })
        );
      })
    ).subscribe(messages => {
      if (messages.length > 0 && this.messages.length < messages.length) {
        this.messages = messages;
        this.scrollToBottom();
      }
    });
  }

  protected sendMessage(): void {
    if (this.isMessageInvalid() || !this.group) {
      return; // Exit early if the message is invalid or group is not loaded
    }

    const messageReq: MessageRequest = {
      message: this.messageForm.value.message
    };

    this.groupSvc.sendMessage(this.group.id, messageReq).subscribe({
      next: (response) => {
        // Add the new message to the array and clear the form
        this.messageForm.reset();
        this.loadGroupMessages(); // Refresh messages
      },
      error: (error) => {
        console.error('Error sending message:', error);
        this.snackBar.open('Failed to send message', 'Close', { duration: 3000 });
      }
    });
  }

  protected approveJoinRequest(userId: number): void {
    if (!this.group || !this.isCreator) return;

    this.groupSvc.approveJoinRequest(this.group.id, userId).subscribe({
      next: () => {
        this.snackBar.open('Join request approved', 'Close', { duration: 3000 });
        this.loadGroupDetails(); // Refresh group data
        this.loadPendingRequests(); // Refresh pending requests
      },
      error: (error) => {
        console.error('Error approving request:', error);
        this.snackBar.open('Failed to approve request', 'Close', { duration: 3000 });
      }
    });
  }

  protected rejectJoinRequest(userId: number): void {
    if (!this.group || !this.isCreator) return;

    this.groupSvc.rejectJoinRequest(this.group.id, userId).subscribe({
      next: () => {
        this.snackBar.open('Join request rejected', 'Close', { duration: 3000 });
        this.loadPendingRequests(); // Refresh pending requests
      },
      error: (error) => {
        console.error('Error rejecting request:', error);
        this.snackBar.open('Failed to reject request', 'Close', { duration: 3000 });
      }
    });
  }

  protected leaveGroup(): void {
    if (!this.group) return;

    this.groupSvc.leaveGroup(this.group.id).subscribe({
      next: () => {
        this.snackBar.open('You have left the group', 'Close', { duration: 3000 });
        this.router.navigate(['/groups']);
      },
      error: (error) => {
        console.error('Error leaving group:', error);
        this.snackBar.open('Failed to leave group', 'Close', { duration: 3000 });
      }
    });
  }

  protected joinGroup(): void {
    if (!this.group) return;

    this.groupSvc.joinGroup(this.group.id).subscribe({
      next: () => {
        this.snackBar.open('Join request submitted', 'Close', { duration: 3000 });
        this.currentUserRole = 'pending';
      },
      error: (error) => {
        console.error('Error joining group:', error);
        this.snackBar.open('Failed to submit join request', 'Close', { duration: 3000 });
      }
    });
  }

  protected toggleSection(section: 'chat' | 'members' | 'requests'): void {
    if (section === 'chat') {
      this.showChatSection = !this.showChatSection;
      if (this.showChatSection) {
        setTimeout(() => this.scrollToBottom(), 100);
      }
    } else if (section === 'members') {
      this.showMembersSection = !this.showMembersSection;
    } else if (section === 'requests') {
      this.showRequestsSection = !this.showRequestsSection;
    }
  }

  private scrollToBottom(): void {
    setTimeout(() => {
      const chatContainer = document.querySelector('.chat-messages');
      if (chatContainer) {
        chatContainer.scrollTop = chatContainer.scrollHeight;
      }
    }, 100);
  }

  protected formatDate(timestamp: string): string {
    // Create date object from timestamp - this preserves the timezone information
    const date = new Date(timestamp + 'Z'); // force to UTC timezone
    
    // Create today and yesterday dates in the user's local timezone
    const today = new Date();
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);
    
    // Check if the message date is today or yesterday (comparing only the date portion)
    const messageDate = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    const todayDate = new Date(today.getFullYear(), today.getMonth(), today.getDate());
    const yesterdayDate = new Date(yesterday.getFullYear(), yesterday.getMonth(), yesterday.getDate());
    
    // Format the time portion consistently
    const timeString = date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    
    if (messageDate.getTime() === todayDate.getTime()) {
      return `Today, ${timeString}`;
    } else if (messageDate.getTime() === yesterdayDate.getTime()) {
      return `Yesterday, ${timeString}`;
    } else {
      // For other dates, use full localized format
      return `${date.toLocaleDateString()} ${timeString}`;
    }
  }

  protected goBackToGroups(): void {
    this.router.navigate(['/groups']);
  }

}
